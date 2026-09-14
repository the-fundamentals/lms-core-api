package tech.sangdang.lmscoreapi.modules.management.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.helpers.SecurityTestSupport.adminJwt;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.classroom;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.END_TIME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.END_TIME_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.START_TIME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.START_TIME_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleOccurrenceFixtures.CANCELLED_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleOccurrenceFixtures.OCCURRENCE_DATE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleOccurrenceFixtures.OCCURRENCE_DATE_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleOccurrenceFixtures.classroomScheduleCancelled;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleCancelledCommand;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomScheduleCancelledServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomScheduleCancelledMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleCancelled;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomScheduleCancelledRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomScheduleCancelledController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomScheduleCancelledServiceImpl.class,
  ClassroomScheduleCancelledMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom schedule cancelled occurrences")
class ClassroomScheduleCancelledControllerIntegrationTest {

  private static final UUID OTHER_CLASSROOM_ID =
      UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomScheduleCancelledRepository classroomScheduleCancelledRepository;

  @Test
  @DisplayName("creates a cancelled classroom schedule occurrence")
  void createClassroomScheduleCancelled_valid_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomScheduleCancelledRepository.insert(any(ClassroomScheduleCancelled.class)))
        .thenAnswer(
            invocation -> {
              ClassroomScheduleCancelled incoming = invocation.getArgument(0);
              return classroomScheduleCancelled(CANCELLED_ID, incoming.getClassroomId())
                  .setDate(incoming.getDate())
                  .setStartTime(incoming.getStartTime())
                  .setEndTime(incoming.getEndTime());
            });

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/schedule-cancelled", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createCancelledCommand()))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(CANCELLED_ID.toString()))
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.date").value(OCCURRENCE_DATE_VALUE))
        .andExpect(jsonPath("$.startTime").value(START_TIME_VALUE))
        .andExpect(jsonPath("$.endTime").value(END_TIME_VALUE))
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());

    ArgumentCaptor<ClassroomScheduleCancelled> captor =
        ArgumentCaptor.forClass(ClassroomScheduleCancelled.class);
    verify(classroomScheduleCancelledRepository).insert(captor.capture());
    assertThat(captor.getValue().getClassroomId()).isEqualTo(CLASSROOM_ID);
    assertThat(captor.getValue().getDate()).isEqualTo(OCCURRENCE_DATE);
    assertThat(captor.getValue().getStartTime()).isEqualTo(START_TIME);
    assertThat(captor.getValue().getEndTime()).isEqualTo(END_TIME);
  }

  @Test
  @DisplayName("gets all cancelled classroom schedule occurrences")
  void getAllClassroomScheduleCancelleds_valid_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomScheduleCancelledRepository.findByClassroomId(CLASSROOM_ID, null, null))
        .thenReturn(List.of(classroomScheduleCancelled()));

    mockMvc
        .perform(
            get("/admin/classrooms/{classroomId}/schedule-cancelled", CLASSROOM_ID)
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(CANCELLED_ID.toString()))
        .andExpect(jsonPath("$[0].classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$[0].date").value(OCCURRENCE_DATE_VALUE))
        .andExpect(jsonPath("$[0].startTime").value(START_TIME_VALUE))
        .andExpect(jsonPath("$[0].endTime").value(END_TIME_VALUE));

    verify(classroomScheduleCancelledRepository).findByClassroomId(CLASSROOM_ID, null, null);
  }

  @Test
  @DisplayName("gets cancelled occurrences filtered by inclusive date range")
  void getAllClassroomScheduleCancelleds_dateRange_returns200() throws Exception {
    LocalDate startDate = OCCURRENCE_DATE.minusDays(1);
    LocalDate endDate = OCCURRENCE_DATE.plusDays(1);
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomScheduleCancelledRepository.findByClassroomId(CLASSROOM_ID, startDate, endDate))
        .thenReturn(List.of(classroomScheduleCancelled()));

    mockMvc
        .perform(
            get("/admin/classrooms/{classroomId}/schedule-cancelled", CLASSROOM_ID)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(CANCELLED_ID.toString()));

    verify(classroomScheduleCancelledRepository)
        .findByClassroomId(CLASSROOM_ID, startDate, endDate);
  }

  @Test
  @DisplayName("rejects getting cancelled occurrences when startDate is after endDate")
  void getAllClassroomScheduleCancelleds_startAfterEnd_returns400() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));

    mockMvc
        .perform(
            get("/admin/classrooms/{classroomId}/schedule-cancelled", CLASSROOM_ID)
                .param("startDate", "2026-07-31")
                .param("endDate", "2026-07-01")
                .with(adminJwt()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_DATE_RANGE"));

    verify(classroomScheduleCancelledRepository, never()).findByClassroomId(any(), any(), any());
  }

  @Test
  @DisplayName("deletes a cancelled classroom schedule occurrence")
  void deleteClassroomScheduleCancelled_valid_returns204() throws Exception {
    when(classroomScheduleCancelledRepository.findById(CANCELLED_ID))
        .thenReturn(Optional.of(classroomScheduleCancelled()));

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/schedule-cancelled/{cancelledId}",
                    CLASSROOM_ID,
                    CANCELLED_ID)
                .with(adminJwt()))
        .andExpect(status().isNoContent());

    verify(classroomScheduleCancelledRepository).deleteById(CANCELLED_ID);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to create a cancelled occurrence when classroom does not exist, POST",
    "fails to get cancelled occurrences when classroom does not exist, GET"
  })
  void classroomLookup_failsWhenMissing(String displayName, String httpMethod) throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    MockHttpServletRequestBuilder request =
        switch (httpMethod) {
          case "POST" ->
              post("/admin/classrooms/{classroomId}/schedule-cancelled", CLASSROOM_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(jsonMapper.writeValueAsString(createCancelledCommand()));
          case "GET" -> get("/admin/classrooms/{classroomId}/schedule-cancelled", CLASSROOM_ID);
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request.with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));

    verify(classroomScheduleCancelledRepository, never()).insert(any());
    verify(classroomScheduleCancelledRepository, never()).findByClassroomId(any(), any(), any());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to delete a cancelled occurrence that does not exist, MISSING",
    "fails to delete a cancelled occurrence that belongs to another classroom, WRONG_CLASSROOM"
  })
  void cancelledLookup_failsWhenUnavailable(String displayName, String occurrenceState)
      throws Exception {
    when(classroomScheduleCancelledRepository.findById(CANCELLED_ID))
        .thenReturn(
            switch (occurrenceState) {
              case "MISSING" -> Optional.empty();
              case "WRONG_CLASSROOM" ->
                  Optional.of(classroomScheduleCancelled(CANCELLED_ID, OTHER_CLASSROOM_ID));
              default ->
                  throw new IllegalArgumentException("Unsupported state: " + occurrenceState);
            });

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/schedule-cancelled/{cancelledId}",
                    CLASSROOM_ID,
                    CANCELLED_ID)
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_SCHEDULE_CANCELLED_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));

    verify(classroomScheduleCancelledRepository, never()).deleteById(any());
  }

  private static CreateClassroomScheduleCancelledCommand createCancelledCommand() {
    return CreateClassroomScheduleCancelledCommand.builder()
        .date(OCCURRENCE_DATE)
        .startTime(START_TIME_VALUE)
        .endTime(END_TIME_VALUE)
        .build();
  }
}
