package tech.sangdang.lmscoreapi.modules.management.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.helpers.SecurityTestSupport.adminJwt;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.classroom;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.GetClassroomRevenueQuery;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomRevenueServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomSessionRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomFinancesController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomRevenueServiceImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom finances")
class ClassroomFinancesControllerIntegrationTest {

  private static final OffsetDateTime FROM =
      OffsetDateTime.of(2026, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);
  private static final OffsetDateTime TO =
      OffsetDateTime.of(2026, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC);
  private static final LocalDateTime FROM_LOCAL = FROM.toLocalDateTime();
  private static final LocalDateTime TO_LOCAL = TO.toLocalDateTime();
  private static final long REVENUE = 1_000_000L;

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomSessionRepository classroomSessionRepository;

  @Test
  @DisplayName("returns classroom revenue for a valid range")
  void getClassroomRevenue_validRange_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomSessionRepository.sumRevenue(CLASSROOM_ID, FROM_LOCAL, TO_LOCAL))
        .thenReturn(REVENUE);

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/revenue/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(query(FROM, TO)))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.revenue").value(REVENUE))
        .andExpect(jsonPath("$.currency").value("VND"));

    verify(classroomSessionRepository).sumRevenue(eq(CLASSROOM_ID), eq(FROM_LOCAL), eq(TO_LOCAL));
  }

  @Test
  @DisplayName("returns zero revenue when nothing matches the range")
  void getClassroomRevenue_empty_returns200WithZero() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomSessionRepository.sumRevenue(CLASSROOM_ID, FROM_LOCAL, TO_LOCAL)).thenReturn(0L);

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/revenue/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(query(FROM, TO)))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.revenue").value(0))
        .andExpect(jsonPath("$.currency").value("VND"));
  }

  @Test
  @DisplayName("fails to get revenue when the classroom does not exist")
  void getClassroomRevenue_classroomNotFound_returns404() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/revenue/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(query(FROM, TO)))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"));

    verify(classroomSessionRepository, never())
        .sumRevenue(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails when from equals to, 2026-08-01T00:00:00Z, 2026-08-01T00:00:00Z",
    "fails when from is after to, 2026-09-01T00:00:00Z, 2026-08-01T00:00:00Z"
  })
  @DisplayName("fails to get revenue when the range is invalid")
  void getClassroomRevenue_invalidRange_returns400(String unused, String from, String to)
      throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/revenue/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        query(OffsetDateTime.parse(from), OffsetDateTime.parse(to))))
                .with(adminJwt()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REVENUE_RANGE"));

    verify(classroomSessionRepository, never())
        .sumRevenue(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
  }

  private static GetClassroomRevenueQuery query(OffsetDateTime from, OffsetDateTime to) {
    return GetClassroomRevenueQuery.builder().from(from).to(to).build();
  }
}
