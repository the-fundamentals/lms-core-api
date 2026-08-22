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
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.INVALID_SCHEDULE_RULE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.SCHEDULE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.SCHEDULE_RULE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.classroomSchedule;

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
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleCommand;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomScheduleServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomScheduleMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSchedule;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomScheduleRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomScheduleController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomScheduleServiceImpl.class,
  ClassroomScheduleMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom schedule management")
class ClassroomScheduleControllerIntegrationTest {

  private static final UUID OTHER_CLASSROOM_ID =
      UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomScheduleRepository classroomScheduleRepository;

  @Test
  @DisplayName("creates a classroom schedule")
  void createClassroomSchedule_valid_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomScheduleRepository.insert(any(ClassroomSchedule.class)))
        .thenAnswer(
            invocation -> {
              ClassroomSchedule incoming = invocation.getArgument(0);
              return classroomSchedule(
                  SCHEDULE_ID, incoming.getClassroomId(), incoming.getScheduleRule());
            });

    CreateClassroomScheduleCommand command =
        CreateClassroomScheduleCommand.builder().scheduleRule(SCHEDULE_RULE).build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(SCHEDULE_ID.toString()))
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.scheduleRule").value(SCHEDULE_RULE))
        .andExpect(jsonPath("$.deletedDate").doesNotExist())
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());

    ArgumentCaptor<ClassroomSchedule> captor = ArgumentCaptor.forClass(ClassroomSchedule.class);
    verify(classroomScheduleRepository).insert(captor.capture());
    assertThat(captor.getValue().getClassroomId()).isEqualTo(CLASSROOM_ID);
    assertThat(captor.getValue().getScheduleRule()).isEqualTo(SCHEDULE_RULE);
  }

  @Test
  @DisplayName("rejects creating a schedule with an invalid recurrence rule")
  void createClassroomSchedule_invalidRrule_returns400() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));

    CreateClassroomScheduleCommand command =
        CreateClassroomScheduleCommand.builder().scheduleRule(INVALID_SCHEDULE_RULE).build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_RRULE"));

    verify(classroomScheduleRepository, never()).insert(any());
  }

  @Test
  @DisplayName("gets all non-deleted classroom schedules")
  void getAllClassroomSchedules_valid_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomScheduleRepository.findByClassroomIdAndDeletedDateIsNull(CLASSROOM_ID))
        .thenReturn(List.of(classroomSchedule()));

    mockMvc
        .perform(get("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID).with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(SCHEDULE_ID.toString()))
        .andExpect(jsonPath("$[0].classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$[0].scheduleRule").value(SCHEDULE_RULE));

    verify(classroomScheduleRepository).findByClassroomIdAndDeletedDateIsNull(CLASSROOM_ID);
  }

  @Test
  @DisplayName("soft-deletes a classroom schedule")
  void deleteClassroomSchedule_valid_returns204() throws Exception {
    ClassroomSchedule schedule = classroomSchedule();
    when(classroomScheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
    when(classroomScheduleRepository.update(any(ClassroomSchedule.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/schedule/{scheduleId}",
                    CLASSROOM_ID,
                    SCHEDULE_ID)
                .with(adminJwt()))
        .andExpect(status().isNoContent());

    ArgumentCaptor<ClassroomSchedule> captor = ArgumentCaptor.forClass(ClassroomSchedule.class);
    verify(classroomScheduleRepository).update(captor.capture());
    assertThat(captor.getValue().getDeletedDate()).isNotNull();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to create a schedule when classroom does not exist, POST",
    "fails to get schedules when classroom does not exist, GET"
  })
  void classroomLookup_failsWhenMissing(String displayName, String httpMethod) throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    MockHttpServletRequestBuilder request =
        switch (httpMethod) {
          case "POST" ->
              post("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      jsonMapper.writeValueAsString(
                          CreateClassroomScheduleCommand.builder()
                              .scheduleRule(SCHEDULE_RULE)
                              .build()));
          case "GET" -> get("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID);
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request.with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));

    verify(classroomScheduleRepository, never()).insert(any());
    verify(classroomScheduleRepository, never()).findByClassroomIdAndDeletedDateIsNull(any());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to delete a schedule that does not exist, MISSING",
    "fails to delete a schedule that belongs to another classroom, WRONG_CLASSROOM"
  })
  void scheduleLookup_failsWhenUnavailable(String displayName, String scheduleState)
      throws Exception {
    when(classroomScheduleRepository.findById(SCHEDULE_ID))
        .thenReturn(
            switch (scheduleState) {
              case "MISSING" -> Optional.empty();
              case "WRONG_CLASSROOM" ->
                  Optional.of(classroomSchedule(SCHEDULE_ID, OTHER_CLASSROOM_ID, SCHEDULE_RULE));
              default -> throw new IllegalArgumentException("Unsupported state: " + scheduleState);
            });

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/schedule/{scheduleId}",
                    CLASSROOM_ID,
                    SCHEDULE_ID)
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_SCHEDULE_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));

    verify(classroomScheduleRepository, never()).update(any());
  }
}
