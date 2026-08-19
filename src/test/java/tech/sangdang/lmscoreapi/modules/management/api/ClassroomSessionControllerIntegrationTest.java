package tech.sangdang.lmscoreapi.modules.management.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.ACCOUNT_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.SECOND_ACCOUNT_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.SECOND_MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.classroomMember;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.ATTENDANCE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.SECOND_ATTENDANCE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.SESSION_DATE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.SESSION_DESCRIPTION;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.SESSION_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.SESSION_NAME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.classroomSession;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.classroomSessionAttendance;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceStatus;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendanceCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendancesCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionCommand;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomSessionServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomSessionAttendanceMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomSessionMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendance;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomSessionAttendanceRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomSessionRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomSessionController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomSessionServiceImpl.class,
  ClassroomSessionMapperImpl.class,
  ClassroomSessionAttendanceMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom session management")
class ClassroomSessionControllerIntegrationTest {

  private static final UUID OTHER_CLASSROOM_ID =
      UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomSessionRepository classroomSessionRepository;
  @MockitoBean private ClassroomMemberRepository classroomMemberRepository;
  @MockitoBean private ClassroomSessionAttendanceRepository classroomSessionAttendanceRepository;

  @Test
  @DisplayName("creates a classroom session")
  void createClassroomSession_valid_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomSessionRepository.insert(any(ClassroomSession.class)))
        .thenAnswer(
            invocation -> {
              ClassroomSession incoming = invocation.getArgument(0);
              return classroomSession(
                      SESSION_ID, incoming.getClassroomId(), incoming.getSessionDate())
                  .setName(incoming.getName())
                  .setDescription(incoming.getDescription());
            });

    CreateClassroomSessionCommand command =
        CreateClassroomSessionCommand.builder()
            .sessionDate(SESSION_DATE.atOffset(ZoneOffset.UTC))
            .name(SESSION_NAME)
            .description(SESSION_DESCRIPTION)
            .build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/sessions", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.sessionDate").exists())
        .andExpect(jsonPath("$.name").value(SESSION_NAME))
        .andExpect(jsonPath("$.description").value(SESSION_DESCRIPTION))
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());

    ArgumentCaptor<ClassroomSession> captor = ArgumentCaptor.forClass(ClassroomSession.class);
    verify(classroomSessionRepository).insert(captor.capture());
    assertThat(captor.getValue().getClassroomId()).isEqualTo(CLASSROOM_ID);
    assertThat(captor.getValue().getSessionDate()).isEqualTo(SESSION_DATE);
    assertThat(captor.getValue().getName()).isEqualTo(SESSION_NAME);
    assertThat(captor.getValue().getDescription()).isEqualTo(SESSION_DESCRIPTION);
  }

  @Test
  @DisplayName("creates a classroom session without a name or description")
  void createClassroomSession_withoutOptionalFields_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomSessionRepository.insert(any(ClassroomSession.class)))
        .thenAnswer(
            invocation -> {
              ClassroomSession incoming = invocation.getArgument(0);
              return classroomSession(
                      SESSION_ID, incoming.getClassroomId(), incoming.getSessionDate())
                  .setName(incoming.getName())
                  .setDescription(incoming.getDescription());
            });

    CreateClassroomSessionCommand command =
        CreateClassroomSessionCommand.builder()
            .sessionDate(SESSION_DATE.atOffset(ZoneOffset.UTC))
            .build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/sessions", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$.name").doesNotExist())
        .andExpect(jsonPath("$.description").doesNotExist());

    ArgumentCaptor<ClassroomSession> captor = ArgumentCaptor.forClass(ClassroomSession.class);
    verify(classroomSessionRepository).insert(captor.capture());
    assertThat(captor.getValue().getName()).isNull();
    assertThat(captor.getValue().getDescription()).isNull();
  }

  @Test
  @DisplayName("fails to create a session when the classroom does not exist")
  void createClassroomSession_classroomNotFound_returns404() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    CreateClassroomSessionCommand command =
        CreateClassroomSessionCommand.builder()
            .sessionDate(SESSION_DATE.atOffset(ZoneOffset.UTC))
            .build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/sessions", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"));

    verify(classroomSessionRepository, never()).insert(any());
  }

  @Test
  @DisplayName("gets a classroom session by id")
  void getClassroomSessionById_found_returns200() throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(Optional.of(classroomSession()));

    mockMvc
        .perform(
            get("/admin/classrooms/{classroomId}/sessions/{sessionId}", CLASSROOM_ID, SESSION_ID)
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.sessionDate").exists())
        .andExpect(jsonPath("$.name").value(SESSION_NAME))
        .andExpect(jsonPath("$.description").value(SESSION_DESCRIPTION));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to get a session that does not exist, GET, MISSING",
    "fails to get a session that belongs to another classroom, GET, WRONG_CLASSROOM",
    "fails to delete a session that does not exist, DELETE, MISSING",
    "fails to delete a session that belongs to another classroom, DELETE, WRONG_CLASSROOM"
  })
  void sessionLookup_failsWhenUnavailable(
      String displayName, String httpMethod, String sessionState) throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(
            switch (sessionState) {
              case "MISSING" -> Optional.empty();
              case "WRONG_CLASSROOM" ->
                  Optional.of(classroomSession(SESSION_ID, OTHER_CLASSROOM_ID, SESSION_DATE));
              default -> throw new IllegalArgumentException("Unsupported state: " + sessionState);
            });

    MockHttpServletRequestBuilder request =
        switch (httpMethod) {
          case "GET" ->
              get("/admin/classrooms/{classroomId}/sessions/{sessionId}", CLASSROOM_ID, SESSION_ID);
          case "DELETE" ->
              delete(
                  "/admin/classrooms/{classroomId}/sessions/{sessionId}", CLASSROOM_ID, SESSION_ID);
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request.with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_SESSION_NOT_FOUND"));

    verify(classroomSessionRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("queries classroom sessions")
  void getAllClassroomSessions_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomSessionRepository.query(any(BaseQuery.class)))
        .thenReturn(Stream.of(classroomSession()));

    ClassroomSessionFilter filter = ClassroomSessionFilter.builder().build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/sessions/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$[0].classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$[0].name").value(SESSION_NAME))
        .andExpect(jsonPath("$[0].description").value(SESSION_DESCRIPTION));

    ArgumentCaptor<BaseQuery> queryCaptor = ArgumentCaptor.forClass(BaseQuery.class);
    verify(classroomSessionRepository).query(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getFilters())
        .anyMatch(
            f ->
                "classroomId".equals(f.getField()) && CLASSROOM_ID.toString().equals(f.getValue()));
  }

  @Test
  @DisplayName("fails to query sessions when the classroom does not exist")
  void getAllClassroomSessions_classroomNotFound_returns404() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    ClassroomSessionFilter filter = ClassroomSessionFilter.builder().build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/sessions/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"));

    verify(classroomSessionRepository, never()).query(any());
  }

  @Test
  @DisplayName("queries classroom session attendances for a member")
  void getClassroomMemberAttendances_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomSessionAttendanceRepository.query(any(BaseQuery.class)))
        .thenReturn(Stream.of(classroomSessionAttendance()));

    ClassroomSessionAttendanceFilter filter = ClassroomSessionAttendanceFilter.builder().build();

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/members/{memberId}/attendances/query",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(ATTENDANCE_ID.toString()))
        .andExpect(jsonPath("$[0].sessionId").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$[0].classroomMemberId").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$[0].status").value("ATTENDED"));

    ArgumentCaptor<BaseQuery> queryCaptor = ArgumentCaptor.forClass(BaseQuery.class);
    verify(classroomSessionAttendanceRepository).query(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getFilters())
        .anyMatch(
            f ->
                "classroomMemberId".equals(f.getField())
                    && MEMBER_ID.toString().equals(f.getValue()));
  }

  @Test
  @DisplayName("queries attendance history for a previously removed classroom member")
  void getClassroomMemberAttendances_removedMember_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomMemberRepository.findById(MEMBER_ID))
        .thenReturn(
            Optional.of(
                classroomMember(
                    MEMBER_ID, CLASSROOM_ID, ACCOUNT_ID, ClassroomMemberStatus.REMOVED)));
    when(classroomSessionAttendanceRepository.query(any(BaseQuery.class)))
        .thenReturn(Stream.of(classroomSessionAttendance()));

    ClassroomSessionAttendanceFilter filter = ClassroomSessionAttendanceFilter.builder().build();

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/members/{memberId}/attendances/query",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    verify(classroomSessionAttendanceRepository).query(any(BaseQuery.class));
  }

  @Test
  @DisplayName("fails to query member attendances when the classroom does not exist")
  void getClassroomMemberAttendances_classroomNotFound_returns404() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    ClassroomSessionAttendanceFilter filter = ClassroomSessionAttendanceFilter.builder().build();

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/members/{memberId}/attendances/query",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"));

    verify(classroomSessionAttendanceRepository, never()).query(any());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to query attendances when the member does not exist, MISSING",
    "fails to query attendances when the member belongs to another classroom, WRONG_CLASSROOM"
  })
  void getClassroomMemberAttendances_memberUnavailable_returns404(
      String displayName, String memberState) throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomMemberRepository.findById(MEMBER_ID))
        .thenReturn(
            switch (memberState) {
              case "MISSING" -> Optional.empty();
              case "WRONG_CLASSROOM" ->
                  Optional.of(
                      classroomMember(
                          MEMBER_ID, OTHER_CLASSROOM_ID, ACCOUNT_ID, ClassroomMemberStatus.ACTIVE));
              default -> throw new IllegalArgumentException("Unsupported state: " + memberState);
            });

    ClassroomSessionAttendanceFilter filter = ClassroomSessionAttendanceFilter.builder().build();

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/members/{memberId}/attendances/query",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_MEMBER_NOT_FOUND"));

    verify(classroomSessionAttendanceRepository, never()).query(any());
  }

  @Test
  @DisplayName("gets all attendances for a classroom session")
  void getAllClassroomSessionAttendances_returns200() throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(Optional.of(classroomSession()));
    when(classroomSessionAttendanceRepository.findBySessionId(SESSION_ID))
        .thenReturn(List.of(classroomSessionAttendance()));

    mockMvc
        .perform(
            get(
                    "/admin/classrooms/{classroomId}/sessions/{sessionId}/attendances",
                    CLASSROOM_ID,
                    SESSION_ID)
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(ATTENDANCE_ID.toString()))
        .andExpect(jsonPath("$[0].sessionId").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$[0].classroomMemberId").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$[0].status").value("ATTENDED"));

    verify(classroomSessionAttendanceRepository).findBySessionId(SESSION_ID);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to get session attendances when the session does not exist, MISSING",
    "fails to get session attendances when the session belongs to another classroom, WRONG_CLASSROOM"
  })
  void getAllClassroomSessionAttendances_sessionUnavailable_returns404(
      String displayName, String sessionState) throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(
            switch (sessionState) {
              case "MISSING" -> Optional.empty();
              case "WRONG_CLASSROOM" ->
                  Optional.of(classroomSession(SESSION_ID, OTHER_CLASSROOM_ID, SESSION_DATE));
              default -> throw new IllegalArgumentException("Unsupported state: " + sessionState);
            });

    mockMvc
        .perform(
            get(
                    "/admin/classrooms/{classroomId}/sessions/{sessionId}/attendances",
                    CLASSROOM_ID,
                    SESSION_ID)
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_SESSION_NOT_FOUND"));

    verify(classroomSessionAttendanceRepository, never()).findBySessionId(any());
  }

  @Test
  @DisplayName("deletes a classroom session")
  void deleteClassroomSession_valid_returns204() throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(Optional.of(classroomSession()));

    mockMvc
        .perform(
            delete("/admin/classrooms/{classroomId}/sessions/{sessionId}", CLASSROOM_ID, SESSION_ID)
                .with(adminJwt()))
        .andExpect(status().isNoContent());

    verify(classroomSessionRepository).deleteById(SESSION_ID);
  }

  @Test
  @DisplayName("creates a classroom session attendance")
  void createClassroomSessionAttendances_valid_returns201() throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(Optional.of(classroomSession()));
    when(classroomMemberRepository.findAllById(any())).thenReturn(List.of(classroomMember()));
    when(classroomSessionAttendanceRepository.findBySessionIdAndClassroomMemberIdIn(
            any(), any()))
        .thenReturn(List.of());
    stubInsertAll();

    CreateClassroomSessionAttendancesCommand command = attendancesCommand(attendanceItem(MEMBER_ID));

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/sessions/{sessionId}/attendances",
                    CLASSROOM_ID,
                    SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(ATTENDANCE_ID.toString()))
        .andExpect(jsonPath("$[0].sessionId").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$[0].classroomMemberId").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$[0].status").value("ATTENDED"))
        .andExpect(jsonPath("$[0].attendanceDate").exists());

    ArgumentCaptor<Iterable<ClassroomSessionAttendance>> captor = iterableCaptor();
    verify(classroomSessionAttendanceRepository).insertAll(captor.capture());
    ClassroomSessionAttendance saved = only(captor.getValue());
    assertThat(saved.getSessionId()).isEqualTo(SESSION_ID);
    assertThat(saved.getClassroomMemberId()).isEqualTo(MEMBER_ID);
    assertThat(saved.getStatus())
        .isEqualTo(
            tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendanceStatus
                .ATTENDED);
  }

  @Test
  @DisplayName("creates classroom session attendances for multiple members in one request")
  void createClassroomSessionAttendances_multipleMembers_returns201() throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(Optional.of(classroomSession()));
    when(classroomMemberRepository.findAllById(any()))
        .thenReturn(
            List.of(
                classroomMember(),
                classroomMember(
                    SECOND_MEMBER_ID, CLASSROOM_ID, SECOND_ACCOUNT_ID, ClassroomMemberStatus.ACTIVE)));
    when(classroomSessionAttendanceRepository.findBySessionIdAndClassroomMemberIdIn(
            any(), any()))
        .thenReturn(List.of());
    stubInsertAll();

    CreateClassroomSessionAttendancesCommand command =
        attendancesCommand(attendanceItem(MEMBER_ID), attendanceItem(SECOND_MEMBER_ID));

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/sessions/{sessionId}/attendances",
                    CLASSROOM_ID,
                    SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].classroomMemberId").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$[1].classroomMemberId").value(SECOND_MEMBER_ID.toString()));

    verify(classroomMemberRepository, times(1)).findAllById(any());
    verify(classroomSessionAttendanceRepository, times(1)).insertAll(any());
  }

  @Test
  @DisplayName("rejects duplicate classroom member ids in one create request")
  void createClassroomSessionAttendances_duplicateMemberId_returns400() throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(Optional.of(classroomSession()));

    CreateClassroomSessionAttendancesCommand command =
        attendancesCommand(attendanceItem(MEMBER_ID), attendanceItem(MEMBER_ID));

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/sessions/{sessionId}/attendances",
                    CLASSROOM_ID,
                    SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("DUPLICATE_CLASSROOM_MEMBER_ID"));

    verify(classroomMemberRepository, never()).findAllById(any());
    verify(classroomSessionAttendanceRepository, never()).insertAll(any());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to create attendance when the session does not exist, MISSING_SESSION, ACTIVE, false, 404, CLASSROOM_SESSION_NOT_FOUND",
    "fails to create attendance when the member does not exist, FOUND, MISSING, false, 404, CLASSROOM_MEMBER_NOT_FOUND",
    "rejects creating attendance that already exists, FOUND, ACTIVE, true, 409, CLASSROOM_SESSION_ATTENDANCE_ALREADY_EXISTS"
  })
  void createClassroomSessionAttendances_fails(
      String displayName,
      String sessionState,
      String memberState,
      boolean attendanceExists,
      int httpStatus,
      String errorCode)
      throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(
            switch (sessionState) {
              case "MISSING_SESSION" -> Optional.empty();
              case "FOUND" -> Optional.of(classroomSession());
              default -> throw new IllegalArgumentException("Unsupported state: " + sessionState);
            });

    if ("FOUND".equals(sessionState)) {
      when(classroomMemberRepository.findAllById(any()))
          .thenReturn(
              switch (memberState) {
                case "MISSING" -> List.of();
                case "ACTIVE" -> List.of(classroomMember());
                default -> throw new IllegalArgumentException("Unsupported state: " + memberState);
              });
    }

    if (attendanceExists) {
      when(classroomSessionAttendanceRepository.findBySessionIdAndClassroomMemberIdIn(
              any(), any()))
          .thenReturn(List.of(classroomSessionAttendance()));
    }

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/sessions/{sessionId}/attendances",
                    CLASSROOM_ID,
                    SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(attendancesCommand(attendanceItem(MEMBER_ID))))
                .with(adminJwt()))
        .andExpect(status().is(httpStatus))
        .andExpect(jsonPath("$.code").value(errorCode));

    verify(classroomSessionAttendanceRepository, never()).insertAll(any());
  }

  @Test
  @DisplayName("deletes a classroom session attendance")
  void deleteClassroomSessionAttendance_valid_returns204() throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(Optional.of(classroomSession()));
    when(classroomSessionAttendanceRepository.findById(ATTENDANCE_ID))
        .thenReturn(Optional.of(classroomSessionAttendance()));

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/sessions/{sessionId}/attendances/{attendanceId}",
                    CLASSROOM_ID,
                    SESSION_ID,
                    ATTENDANCE_ID)
                .with(adminJwt()))
        .andExpect(status().isNoContent());

    verify(classroomSessionAttendanceRepository).deleteById(ATTENDANCE_ID);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to delete attendance when the session does not exist, MISSING_SESSION, FOUND",
    "fails to delete attendance that does not exist, FOUND, MISSING",
    "fails to delete attendance that belongs to another session, FOUND, WRONG_SESSION"
  })
  void deleteClassroomSessionAttendance_failsWhenUnavailable(
      String displayName, String sessionState, String attendanceState) throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(
            switch (sessionState) {
              case "MISSING_SESSION" -> Optional.empty();
              case "FOUND" -> Optional.of(classroomSession());
              default -> throw new IllegalArgumentException("Unsupported state: " + sessionState);
            });

    if ("FOUND".equals(sessionState)) {
      UUID otherSessionId = UUID.fromString("22222222-3333-4444-5555-666666666666");
      when(classroomSessionAttendanceRepository.findById(ATTENDANCE_ID))
          .thenReturn(
              switch (attendanceState) {
                case "MISSING" -> Optional.empty();
                case "WRONG_SESSION" ->
                    Optional.of(
                        classroomSessionAttendance(
                            ATTENDANCE_ID,
                            otherSessionId,
                            MEMBER_ID,
                            tech.sangdang.lmscoreapi.modules.management.dom
                                .ClassroomSessionAttendanceStatus.ATTENDED));
                case "FOUND" -> Optional.of(classroomSessionAttendance());
                default ->
                    throw new IllegalArgumentException("Unsupported state: " + attendanceState);
              });
    }

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/sessions/{sessionId}/attendances/{attendanceId}",
                    CLASSROOM_ID,
                    SESSION_ID,
                    ATTENDANCE_ID)
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.code")
                .value(
                    "MISSING_SESSION".equals(sessionState)
                        ? "CLASSROOM_SESSION_NOT_FOUND"
                        : "CLASSROOM_SESSION_ATTENDANCE_NOT_FOUND"));

    verify(classroomSessionAttendanceRepository, never()).deleteById(any());
  }

  private void stubInsertAll() {
    when(classroomSessionAttendanceRepository.insertAll(any()))
        .thenAnswer(
            invocation -> {
              List<ClassroomSessionAttendance> result = new ArrayList<>();
              for (ClassroomSessionAttendance incoming : toList(invocation.getArgument(0))) {
                UUID id =
                    MEMBER_ID.equals(incoming.getClassroomMemberId())
                        ? ATTENDANCE_ID
                        : SECOND_ATTENDANCE_ID;
                result.add(
                    classroomSessionAttendance(
                            id,
                            incoming.getSessionId(),
                            incoming.getClassroomMemberId(),
                            incoming.getStatus())
                        .setAttendanceDate(incoming.getAttendanceDate()));
              }
              return result;
            });
  }

  private static CreateClassroomSessionAttendancesCommand attendancesCommand(
      CreateClassroomSessionAttendanceCommand... items) {
    return CreateClassroomSessionAttendancesCommand.builder().attendances(List.of(items)).build();
  }

  private static CreateClassroomSessionAttendanceCommand attendanceItem(UUID memberId) {
    return CreateClassroomSessionAttendanceCommand.builder()
        .classroomMemberId(memberId)
        .attendanceDate(OffsetDateTime.of(2026, 7, 19, 9, 5, 0, 0, ZoneOffset.UTC))
        .status(ClassroomSessionAttendanceStatus.ATTENDED)
        .build();
  }

  @SuppressWarnings("unchecked")
  private static ArgumentCaptor<Iterable<ClassroomSessionAttendance>> iterableCaptor() {
    return ArgumentCaptor.forClass(Iterable.class);
  }

  private static ClassroomSessionAttendance only(Iterable<ClassroomSessionAttendance> attendances) {
    List<ClassroomSessionAttendance> list = toList(attendances);
    assertThat(list).hasSize(1);
    return list.getFirst();
  }

  private static List<ClassroomSessionAttendance> toList(
      Iterable<ClassroomSessionAttendance> attendances) {
    return StreamSupport.stream(attendances.spliterator(), false).toList();
  }
}
