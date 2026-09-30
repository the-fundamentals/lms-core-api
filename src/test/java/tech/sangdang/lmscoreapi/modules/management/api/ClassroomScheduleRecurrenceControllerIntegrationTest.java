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
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.DESCRIPTION;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.END_TIME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.END_TIME_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.FREQUENCY;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.NAME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.RECURRENCE_START_DATE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.RECURRENCE_START_DATE_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.RECUR_UNTIL;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.RECUR_UNTIL_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.SCHEDULE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.START_TIME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.START_TIME_VALUE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.classroomScheduleRecurrence;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
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
  private static final UUID SECOND_SCHEDULE_ID =
      UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomScheduleRecurrenceRepository classroomScheduleRecurrenceRepository;

  @Test
  @DisplayName("creates classroom schedule recurrences for each weekday")
  void createClassroomScheduleRecurrence_valid_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    stubInsertAll();

    CreateClassroomScheduleRecurrenceCommand command =
        createScheduleCommand(RecurrenceByDay.MONDAY);

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(SCHEDULE_ID.toString()))
        .andExpect(jsonPath("$[0].classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$[0].frequency").value("WEEKLY"))
        .andExpect(jsonPath("$[0].byDay").value("MONDAY"))
        .andExpect(jsonPath("$[0].recurrenceStartDate").value(RECURRENCE_START_DATE_VALUE))
        .andExpect(jsonPath("$[0].recurUntil").value(RECUR_UNTIL_VALUE))
        .andExpect(jsonPath("$[0].startTime").value(START_TIME_VALUE))
        .andExpect(jsonPath("$[0].endTime").value(END_TIME_VALUE))
        .andExpect(jsonPath("$[0].name").value(NAME))
        .andExpect(jsonPath("$[0].description").value(DESCRIPTION))
        .andExpect(jsonPath("$[0].deletedDate").doesNotExist())
        .andExpect(jsonPath("$[0].createdDate").exists())
        .andExpect(jsonPath("$[0].lastModifiedDate").exists());

    ArgumentCaptor<Iterable<ClassroomScheduleRecurrence>> captor = iterableCaptor();
    verify(classroomScheduleRecurrenceRepository).insertAll(captor.capture());
    ClassroomScheduleRecurrence inserted = only(captor.getValue());
    assertThat(inserted.getClassroomId()).isEqualTo(CLASSROOM_ID);
    assertThat(inserted.getFrequency()).isEqualTo(FREQUENCY);
    assertThat(inserted.getByDay()).isEqualTo(BY_DAY);
    assertThat(inserted.getRecurrenceStartDate()).isEqualTo(RECURRENCE_START_DATE);
    assertThat(inserted.getRecurUntil()).isEqualTo(RECUR_UNTIL);
    assertThat(inserted.getStartTime()).isEqualTo(START_TIME);
    assertThat(inserted.getEndTime()).isEqualTo(END_TIME);
    assertThat(inserted.getName()).isEqualTo(NAME);
    assertThat(inserted.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  @DisplayName("creates multiple classroom schedule recurrences in one request")
  void createClassroomScheduleRecurrence_multipleDays_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    stubInsertAll();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        createScheduleCommand(RecurrenceByDay.MONDAY, RecurrenceByDay.WEDNESDAY)))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].byDay").value("MONDAY"))
        .andExpect(jsonPath("$[1].byDay").value("WEDNESDAY"))
        .andExpect(jsonPath("$[0].startTime").value(START_TIME_VALUE))
        .andExpect(jsonPath("$[1].startTime").value(START_TIME_VALUE));

    ArgumentCaptor<Iterable<ClassroomScheduleRecurrence>> captor = iterableCaptor();
    verify(classroomScheduleRecurrenceRepository).insertAll(captor.capture());
    assertThat(captor.getValue())
        .extracting(ClassroomScheduleRecurrence::getByDay)
        .containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    verify(classroomScheduleRecurrenceRepository, never()).insert(any());
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
        .andExpect(jsonPath("$[0].endTime").value(END_TIME_VALUE))
        .andExpect(jsonPath("$[0].name").value(NAME))
        .andExpect(jsonPath("$[0].description").value(DESCRIPTION));

    verify(classroomScheduleRecurrenceRepository)
        .findByClassroomIdAndDeletedDateIsNull(CLASSROOM_ID);
  }

  @Test
  @DisplayName("gets a classroom schedule recurrence by id")
  void getClassroomScheduleRecurrenceById_found_returns200() throws Exception {
    when(classroomScheduleRecurrenceRepository.findById(SCHEDULE_ID))
        .thenReturn(Optional.of(classroomScheduleRecurrence()));

    mockMvc
        .perform(
            get("/admin/classrooms/{classroomId}/schedule/{scheduleId}", CLASSROOM_ID, SCHEDULE_ID)
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(SCHEDULE_ID.toString()))
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.frequency").value("WEEKLY"))
        .andExpect(jsonPath("$.byDay").value("MONDAY"))
        .andExpect(jsonPath("$.recurrenceStartDate").value(RECURRENCE_START_DATE_VALUE))
        .andExpect(jsonPath("$.recurUntil").value(RECUR_UNTIL_VALUE))
        .andExpect(jsonPath("$.startTime").value(START_TIME_VALUE))
        .andExpect(jsonPath("$.endTime").value(END_TIME_VALUE))
        .andExpect(jsonPath("$.name").value(NAME))
        .andExpect(jsonPath("$.description").value(DESCRIPTION));
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
                  .content(
                      jsonMapper.writeValueAsString(createScheduleCommand(RecurrenceByDay.MONDAY)));
          case "GET" -> get("/admin/classrooms/{classroomId}/schedule", CLASSROOM_ID);
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request.with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));

    verify(classroomScheduleRecurrenceRepository, never()).insertAll(any());
    verify(classroomScheduleRecurrenceRepository, never())
        .findByClassroomIdAndDeletedDateIsNull(any());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to get a recurrence that does not exist, GET, MISSING",
    "fails to get a recurrence that belongs to another classroom, GET, WRONG_CLASSROOM",
    "fails to delete a recurrence that does not exist, DELETE, MISSING",
    "fails to delete a recurrence that belongs to another classroom, DELETE, WRONG_CLASSROOM"
  })
  void scheduleLookup_failsWhenUnavailable(
      String displayName, String httpMethod, String scheduleState) throws Exception {
    when(classroomScheduleRecurrenceRepository.findById(SCHEDULE_ID))
        .thenReturn(
            switch (scheduleState) {
              case "MISSING" -> Optional.empty();
              case "WRONG_CLASSROOM" ->
                  Optional.of(classroomScheduleRecurrence(SCHEDULE_ID, OTHER_CLASSROOM_ID));
              default -> throw new IllegalArgumentException("Unsupported state: " + scheduleState);
            });

    MockHttpServletRequestBuilder request =
        switch (httpMethod) {
          case "GET" ->
              get(
                  "/admin/classrooms/{classroomId}/schedule/{scheduleId}",
                  CLASSROOM_ID,
                  SCHEDULE_ID);
          case "DELETE" ->
              delete(
                  "/admin/classrooms/{classroomId}/schedule/{scheduleId}",
                  CLASSROOM_ID,
                  SCHEDULE_ID);
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request.with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_SCHEDULE_RECURRENCE_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));

    verify(classroomScheduleRecurrenceRepository, never()).update(any());
  }

  private void stubInsertAll() {
    when(classroomScheduleRecurrenceRepository.insertAll(any()))
        .thenAnswer(
            invocation -> {
              List<ClassroomScheduleRecurrence> result = new ArrayList<>();
              for (ClassroomScheduleRecurrence incoming : toList(invocation.getArgument(0))) {
                UUID id =
                    incoming.getByDay() == DayOfWeek.MONDAY ? SCHEDULE_ID : SECOND_SCHEDULE_ID;
                result.add(
                    classroomScheduleRecurrence(id, incoming.getClassroomId())
                        .setFrequency(incoming.getFrequency())
                        .setByDay(incoming.getByDay())
                        .setRecurrenceStartDate(incoming.getRecurrenceStartDate())
                        .setRecurUntil(incoming.getRecurUntil())
                        .setStartTime(incoming.getStartTime())
                        .setEndTime(incoming.getEndTime())
                        .setName(incoming.getName())
                        .setDescription(incoming.getDescription()));
              }
              return result;
            });
  }

  private static CreateClassroomScheduleRecurrenceCommand createScheduleCommand(
      RecurrenceByDay... byDays) {
    return CreateClassroomScheduleRecurrenceCommand.builder()
        .frequency(RecurrenceFrequency.WEEKLY)
        // LinkedHashSet: OpenAPI uniqueItems → Set; preserve byDays request order for assertions
        .byDays(new LinkedHashSet<>(Arrays.asList(byDays)))
        .recurrenceStartDate(RECURRENCE_START_DATE)
        .recurUntil(RECUR_UNTIL)
        .startTime(START_TIME)
        .endTime(END_TIME)
        .name(NAME)
        .description(DESCRIPTION)
        .build();
  }

  @SuppressWarnings("unchecked")
  private static ArgumentCaptor<Iterable<ClassroomScheduleRecurrence>> iterableCaptor() {
    return ArgumentCaptor.forClass(Iterable.class);
  }

  private static List<ClassroomScheduleRecurrence> toList(
      Iterable<ClassroomScheduleRecurrence> iterable) {
    List<ClassroomScheduleRecurrence> list = new ArrayList<>();
    iterable.forEach(list::add);
    return list;
  }

  private static ClassroomScheduleRecurrence only(Iterable<ClassroomScheduleRecurrence> iterable) {
    List<ClassroomScheduleRecurrence> list = toList(iterable);
    assertThat(list).hasSize(1);
    return list.getFirst();
  }
}
