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
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.classroom;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.ACCOUNT_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.classroomMember;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.ATTENDANCE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.SESSION_DATE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.SESSION_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.classroomSession;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.classroomSessionAttendance;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
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
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceStatus;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendanceCommand;
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
              return classroomSession(SESSION_ID, incoming.getClassroomId(), incoming.getSessionDate());
            });

    CreateClassroomSessionCommand command =
        CreateClassroomSessionCommand.builder()
            .sessionDate(SESSION_DATE.atOffset(ZoneOffset.UTC))
            .build();

    mockMvc
        .perform(
            post("/classrooms/{classroomId}/sessions", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.sessionDate").exists())
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());

    ArgumentCaptor<ClassroomSession> captor = ArgumentCaptor.forClass(ClassroomSession.class);
    verify(classroomSessionRepository).insert(captor.capture());
    assertThat(captor.getValue().getClassroomId()).isEqualTo(CLASSROOM_ID);
    assertThat(captor.getValue().getSessionDate()).isEqualTo(SESSION_DATE);
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
            post("/classrooms/{classroomId}/sessions", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command)))
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
        .perform(get("/classrooms/{classroomId}/sessions/{sessionId}", CLASSROOM_ID, SESSION_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.sessionDate").exists());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to get a session that does not exist, GET, MISSING",
    "fails to get a session that belongs to another classroom, GET, WRONG_CLASSROOM",
    "fails to delete a session that does not exist, DELETE, MISSING",
    "fails to delete a session that belongs to another classroom, DELETE, WRONG_CLASSROOM"
  })
  void sessionLookup_failsWhenUnavailable(String displayName, String httpMethod, String sessionState)
      throws Exception {
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
              get("/classrooms/{classroomId}/sessions/{sessionId}", CLASSROOM_ID, SESSION_ID);
          case "DELETE" ->
              delete("/classrooms/{classroomId}/sessions/{sessionId}", CLASSROOM_ID, SESSION_ID);
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request)
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
            post("/classrooms/{classroomId}/sessions/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$[0].classroomId").value(CLASSROOM_ID.toString()));

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
            post("/classrooms/{classroomId}/sessions/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"));

    verify(classroomSessionRepository, never()).query(any());
  }

  @Test
  @DisplayName("deletes a classroom session")
  void deleteClassroomSession_valid_returns204() throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(Optional.of(classroomSession()));

    mockMvc
        .perform(delete("/classrooms/{classroomId}/sessions/{sessionId}", CLASSROOM_ID, SESSION_ID))
        .andExpect(status().isNoContent());

    verify(classroomSessionRepository).deleteById(SESSION_ID);
  }

  @Test
  @DisplayName("creates a classroom session attendance")
  void createClassroomSessionAttendance_valid_returns201() throws Exception {
    when(classroomSessionRepository.findById(SESSION_ID))
        .thenReturn(Optional.of(classroomSession()));
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomSessionAttendanceRepository.findBySessionIdAndClassroomMemberId(
            SESSION_ID, MEMBER_ID))
        .thenReturn(Optional.empty());
    when(classroomSessionAttendanceRepository.insert(any(ClassroomSessionAttendance.class)))
        .thenAnswer(
            invocation -> {
              ClassroomSessionAttendance incoming = invocation.getArgument(0);
              return classroomSessionAttendance(
                      ATTENDANCE_ID,
                      incoming.getSessionId(),
                      incoming.getClassroomMemberId(),
                      incoming.getStatus())
                  .setAttendanceDate(incoming.getAttendanceDate());
            });

    CreateClassroomSessionAttendanceCommand command =
        CreateClassroomSessionAttendanceCommand.builder()
            .classroomMemberId(MEMBER_ID)
            .attendanceDate(OffsetDateTime.of(2026, 7, 19, 9, 5, 0, 0, ZoneOffset.UTC))
            .status(ClassroomSessionAttendanceStatus.ATTENDED)
            .build();

    mockMvc
        .perform(
            post(
                    "/classrooms/{classroomId}/sessions/{sessionId}/attendances",
                    CLASSROOM_ID,
                    SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(ATTENDANCE_ID.toString()))
        .andExpect(jsonPath("$.sessionId").value(SESSION_ID.toString()))
        .andExpect(jsonPath("$.classroomMemberId").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$.status").value("ATTENDED"))
        .andExpect(jsonPath("$.attendanceDate").exists());

    ArgumentCaptor<ClassroomSessionAttendance> captor =
        ArgumentCaptor.forClass(ClassroomSessionAttendance.class);
    verify(classroomSessionAttendanceRepository).insert(captor.capture());
    assertThat(captor.getValue().getSessionId()).isEqualTo(SESSION_ID);
    assertThat(captor.getValue().getClassroomMemberId()).isEqualTo(MEMBER_ID);
    assertThat(captor.getValue().getStatus())
        .isEqualTo(
            tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendanceStatus
                .ATTENDED);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to create attendance when the session does not exist, MISSING_SESSION, ACTIVE, false, 404, CLASSROOM_SESSION_NOT_FOUND",
    "fails to create attendance when the member does not exist, FOUND, MISSING, false, 404, CLASSROOM_MEMBER_NOT_FOUND",
    "fails to create attendance when the member is removed, FOUND, REMOVED, false, 404, CLASSROOM_MEMBER_NOT_FOUND",
    "rejects creating attendance that already exists, FOUND, ACTIVE, true, 409, CLASSROOM_SESSION_ATTENDANCE_ALREADY_EXISTS"
  })
  void createClassroomSessionAttendance_fails(
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
      when(classroomMemberRepository.findById(MEMBER_ID))
          .thenReturn(
              switch (memberState) {
                case "MISSING" -> Optional.empty();
                case "REMOVED" ->
                    Optional.of(
                        classroomMember(
                            MEMBER_ID, CLASSROOM_ID, ACCOUNT_ID, ClassroomMemberStatus.REMOVED));
                case "ACTIVE" -> Optional.of(classroomMember());
                default -> throw new IllegalArgumentException("Unsupported state: " + memberState);
              });
    }

    if (attendanceExists) {
      when(classroomSessionAttendanceRepository.findBySessionIdAndClassroomMemberId(
              SESSION_ID, MEMBER_ID))
          .thenReturn(Optional.of(classroomSessionAttendance()));
    }

    CreateClassroomSessionAttendanceCommand command =
        CreateClassroomSessionAttendanceCommand.builder()
            .classroomMemberId(MEMBER_ID)
            .status(ClassroomSessionAttendanceStatus.ATTENDED)
            .build();

    mockMvc
        .perform(
            post(
                    "/classrooms/{classroomId}/sessions/{sessionId}/attendances",
                    CLASSROOM_ID,
                    SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command)))
        .andExpect(status().is(httpStatus))
        .andExpect(jsonPath("$.code").value(errorCode));

    verify(classroomSessionAttendanceRepository, never()).insert(any());
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
                "/classrooms/{classroomId}/sessions/{sessionId}/attendances/{attendanceId}",
                CLASSROOM_ID,
                SESSION_ID,
                ATTENDANCE_ID))
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
                "/classrooms/{classroomId}/sessions/{sessionId}/attendances/{attendanceId}",
                CLASSROOM_ID,
                SESSION_ID,
                ATTENDANCE_ID))
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.code")
                .value(
                    "MISSING_SESSION".equals(sessionState)
                        ? "CLASSROOM_SESSION_NOT_FOUND"
                        : "CLASSROOM_SESSION_ATTENDANCE_NOT_FOUND"));

    verify(classroomSessionAttendanceRepository, never()).deleteById(any());
  }
}
