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
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.BY_DAY;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.END_TIME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.END_TIME_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.FREQUENCY;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.RECURRENCE_START_DATE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.RECURRENCE_START_DATE_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.RECUR_UNTIL;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.RECUR_UNTIL_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.SCHEDULE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.START_TIME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.START_TIME_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.classroomScheduleRecurrence;

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
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleRecurrenceCommand;
import tech.sangdang.lmscoreapi.generated.model.RecurrenceByDay;
import tech.sangdang.lmscoreapi.generated.model.RecurrenceFrequency;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomScheduleRecurrenceServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomScheduleRecurrenceMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleRecurrence;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomScheduleRecurrenceRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomScheduleRecurrenceController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomScheduleRecurrenceServiceImpl.class,
  ClassroomScheduleRecurrenceMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom schedule recurrence management")
class ClassroomScheduleRecurrenceControllerIntegrationTest {

  private static final UUID OTHER_CLASSROOM_ID =
      UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomScheduleRecurrenceRepository classroomScheduleRecurrenceRepository;

  @Test
  @DisplayName("creates a classroom schedule recurrence")
  void createClassroomScheduleRecurrence_valid_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomScheduleRecurrenceRepository.insert(any(ClassroomScheduleRecurrence.class)))
        .thenAnswer(
            invocation -> {
              ClassroomScheduleRecurrence incoming = invocation.getArgument(0);
              return classroomScheduleRecurrence(SCHEDULE_ID, incoming.getClassroomId())
                  .setFrequency(incoming.getFrequency())
                  .setByDay(incoming.getByDay())
                  .setRecurrenceStartDate(incoming.getRecurrenceStartDate())
                  .setRecurUntil(incoming.getRecurUntil())
                  .setStartTime(incoming.getStartTime())
                  .setEndTime(incoming.getEndTime());
            });

    CreateClassroomScheduleRecurrenceCommand command = createScheduleCommand();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(SCHEDULE_ID.toString()))
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.frequency").value("WEEKLY"))
        .andExpect(jsonPath("$.byDay").value("MONDAY"))
        .andExpect(jsonPath("$.recurrenceStartDate").value(RECURRENCE_START_DATE_VALUE))
        .andExpect(jsonPath("$.recurUntil").value(RECUR_UNTIL_VALUE))
        .andExpect(jsonPath("$.startTime").value(START_TIME_VALUE))
        .andExpect(jsonPath("$.endTime").value(END_TIME_VALUE))
        .andExpect(jsonPath("$.deletedDate").doesNotExist())
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());

    ArgumentCaptor<ClassroomScheduleRecurrence> captor =
        ArgumentCaptor.forClass(ClassroomScheduleRecurrence.class);
    verify(classroomScheduleRecurrenceRepository).insert(captor.capture());
    assertThat(captor.getValue().getClassroomId()).isEqualTo(CLASSROOM_ID);
    assertThat(captor.getValue().getFrequency()).isEqualTo(FREQUENCY);
    assertThat(captor.getValue().getByDay()).isEqualTo(BY_DAY);
    assertThat(captor.getValue().getRecurrenceStartDate()).isEqualTo(RECURRENCE_START_DATE);
    assertThat(captor.getValue().getRecurUntil()).isEqualTo(RECUR_UNTIL);
    assertThat(captor.getValue().getStartTime()).isEqualTo(START_TIME);
    assertThat(captor.getValue().getEndTime()).isEqualTo(END_TIME);
  }

  @Test
  @DisplayName("gets all non-deleted classroom schedule recurrences")
  void getAllClassroomScheduleRecurrences_valid_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomScheduleRecurrenceRepository.findByClassroomIdAndDeletedDateIsNull(CLASSROOM_ID))
        .thenReturn(List.of(classroomScheduleRecurrence()));

    mockMvc
        .perform(get("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID).with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(SCHEDULE_ID.toString()))
        .andExpect(jsonPath("$[0].classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$[0].frequency").value("WEEKLY"))
        .andExpect(jsonPath("$[0].byDay").value("MONDAY"))
        .andExpect(jsonPath("$[0].recurrenceStartDate").value(RECURRENCE_START_DATE_VALUE))
        .andExpect(jsonPath("$[0].recurUntil").value(RECUR_UNTIL_VALUE))
        .andExpect(jsonPath("$[0].startTime").value(START_TIME_VALUE))
        .andExpect(jsonPath("$[0].endTime").value(END_TIME_VALUE));

    verify(classroomScheduleRecurrenceRepository)
        .findByClassroomIdAndDeletedDateIsNull(CLASSROOM_ID);
  }

  @Test
  @DisplayName("soft-deletes a classroom schedule recurrence")
  void deleteClassroomScheduleRecurrence_valid_returns204() throws Exception {
    ClassroomScheduleRecurrence recurrence = classroomScheduleRecurrence();
    when(classroomScheduleRecurrenceRepository.findById(SCHEDULE_ID))
        .thenReturn(Optional.of(recurrence));
    when(classroomScheduleRecurrenceRepository.update(any(ClassroomScheduleRecurrence.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/schedule/{scheduleId}",
                    CLASSROOM_ID,
                    SCHEDULE_ID)
                .with(adminJwt()))
        .andExpect(status().isNoContent());

    ArgumentCaptor<ClassroomScheduleRecurrence> captor =
        ArgumentCaptor.forClass(ClassroomScheduleRecurrence.class);
    verify(classroomScheduleRecurrenceRepository).update(captor.capture());
    assertThat(captor.getValue().getDeletedDate()).isNotNull();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to create a recurrence when classroom does not exist, POST",
    "fails to get recurrences when classroom does not exist, GET"
  })
  void classroomLookup_failsWhenMissing(String displayName, String httpMethod) throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    MockHttpServletRequestBuilder request =
        switch (httpMethod) {
          case "POST" ->
              post("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(jsonMapper.writeValueAsString(createScheduleCommand()));
          case "GET" -> get("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID);
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request.with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));

    verify(classroomScheduleRecurrenceRepository, never()).insert(any());
    verify(classroomScheduleRecurrenceRepository, never())
        .findByClassroomIdAndDeletedDateIsNull(any());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to delete a recurrence that does not exist, MISSING",
    "fails to delete a recurrence that belongs to another classroom, WRONG_CLASSROOM"
  })
  void scheduleLookup_failsWhenUnavailable(String displayName, String scheduleState)
      throws Exception {
    when(classroomScheduleRecurrenceRepository.findById(SCHEDULE_ID))
        .thenReturn(
            switch (scheduleState) {
              case "MISSING" -> Optional.empty();
              case "WRONG_CLASSROOM" ->
                  Optional.of(classroomScheduleRecurrence(SCHEDULE_ID, OTHER_CLASSROOM_ID));
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
        .andExpect(jsonPath("$.code").value("CLASSROOM_SCHEDULE_RECURRENCE_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));

    verify(classroomScheduleRecurrenceRepository, never()).update(any());
  }

  private static CreateClassroomScheduleRecurrenceCommand createScheduleCommand() {
    return CreateClassroomScheduleRecurrenceCommand.builder()
        .frequency(RecurrenceFrequency.WEEKLY)
        .byDay(RecurrenceByDay.MONDAY)
        .recurrenceStartDate(RECURRENCE_START_DATE)
        .recurUntil(RECUR_UNTIL)
        .startTime(START_TIME_VALUE)
        .endTime(END_TIME_VALUE)
        .build();
  }
}
