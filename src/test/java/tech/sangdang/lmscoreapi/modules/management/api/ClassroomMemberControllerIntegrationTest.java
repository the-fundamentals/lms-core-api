package tech.sangdang.lmscoreapi.modules.management.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.helpers.SecurityTestSupport.adminJwt;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.classroom;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.ACCOUNT_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_EMAIL;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_NAME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.classroomMember;

import java.util.Optional;
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
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberRole;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomMemberRoleCommand;
import tech.sangdang.lmscoreapi.modules.account.infra.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.infra.AccountProfileCache;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomMemberServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMemberMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomMemberController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomMemberServiceImpl.class,
  ClassroomMemberMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom member management")
class ClassroomMemberControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomMemberRepository classroomMemberRepository;
  @MockitoBean private AccountProfileCache accountProfileCache;

  @Test
  @DisplayName("creates a classroom member")
  void createClassroomMember_valid_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(accountProfileCache.findByAccountId(ACCOUNT_ID))
        .thenReturn(Optional.of(new AccountProfile(ACCOUNT_ID, MEMBER_EMAIL, MEMBER_NAME)));
    when(classroomMemberRepository.findByClassroomIdAndAccountId(CLASSROOM_ID, ACCOUNT_ID))
        .thenReturn(Optional.empty());
    when(classroomMemberRepository.insert(any(ClassroomMember.class)))
        .thenAnswer(
            invocation -> {
              ClassroomMember incoming = invocation.getArgument(0);
              return classroomMember(
                      MEMBER_ID,
                      incoming.getClassroomId(),
                      incoming.getAccountId(),
                      ClassroomMemberStatus.ACTIVE)
                  .setRole(incoming.getRole())
                  .setEmail(incoming.getEmail())
                  .setName(incoming.getName());
            });

    CreateClassroomMemberCommand command =
        CreateClassroomMemberCommand.builder()
            .accountId(ACCOUNT_ID)
            .role(ClassroomMemberRole.STUDENT)
            .build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.accountId").value(ACCOUNT_ID))
        .andExpect(jsonPath("$.role").value("STUDENT"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.email").value(MEMBER_EMAIL))
        .andExpect(jsonPath("$.name").value(MEMBER_NAME));

    ArgumentCaptor<ClassroomMember> captor = ArgumentCaptor.forClass(ClassroomMember.class);
    verify(classroomMemberRepository).insert(captor.capture());
    assertThat(captor.getValue().getAccountId()).isEqualTo(ACCOUNT_ID);
    assertThat(captor.getValue().getEmail()).isEqualTo(MEMBER_EMAIL);
    assertThat(captor.getValue().getName()).isEqualTo(MEMBER_NAME);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to create a member when the classroom does not exist, STUDENT, 404, CLASSROOM_NOT_FOUND, false, false, false",
    "fails to create a member when the account profile is missing, STUDENT, 404, ACCOUNT_PROFILE_NOT_FOUND, true, false, false",
    "rejects creating a member that is already active, TEACHER, 409, CLASSROOM_MEMBER_ALREADY_EXISTS, true, true, true"
  })
  void createClassroomMember_fails(
      String displayName,
      ClassroomMemberRole role,
      int httpStatus,
      String errorCode,
      boolean classroomExists,
      boolean accountExists,
      boolean memberAlreadyActive)
      throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID))
        .thenReturn(classroomExists ? Optional.of(classroom()) : Optional.empty());
    if (classroomExists) {
      when(accountProfileCache.findByAccountId(ACCOUNT_ID))
          .thenReturn(
              accountExists
                  ? Optional.of(new AccountProfile(ACCOUNT_ID, MEMBER_EMAIL, MEMBER_NAME))
                  : Optional.empty());
    }
    if (memberAlreadyActive) {
      when(classroomMemberRepository.findByClassroomIdAndAccountId(CLASSROOM_ID, ACCOUNT_ID))
          .thenReturn(Optional.of(classroomMember()));
    }

    CreateClassroomMemberCommand command =
        CreateClassroomMemberCommand.builder().accountId(ACCOUNT_ID).role(role).build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().is(httpStatus))
        .andExpect(jsonPath("$.code").value(errorCode));

    verify(classroomMemberRepository, never()).insert(any());
    if (memberAlreadyActive) {
      verify(classroomMemberRepository, never()).update(any());
    }
  }

  @Test
  @DisplayName("reactivates a previously removed classroom member")
  void createClassroomMember_reactivatesRemoved_returns201() throws Exception {
    ClassroomMember removed =
        classroomMember(MEMBER_ID, CLASSROOM_ID, ACCOUNT_ID, ClassroomMemberStatus.REMOVED);

    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(accountProfileCache.findByAccountId(ACCOUNT_ID))
        .thenReturn(Optional.of(new AccountProfile(ACCOUNT_ID, MEMBER_EMAIL, MEMBER_NAME)));
    when(classroomMemberRepository.findByClassroomIdAndAccountId(CLASSROOM_ID, ACCOUNT_ID))
        .thenReturn(Optional.of(removed));
    when(classroomMemberRepository.update(any(ClassroomMember.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CreateClassroomMemberCommand command =
        CreateClassroomMemberCommand.builder()
            .accountId(ACCOUNT_ID)
            .role(ClassroomMemberRole.TEACHER)
            .build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$.role").value("TEACHER"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    ArgumentCaptor<ClassroomMember> captor = ArgumentCaptor.forClass(ClassroomMember.class);
    verify(classroomMemberRepository).update(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(ClassroomMemberStatus.ACTIVE);
    assertThat(captor.getValue().getRole())
        .isEqualTo(tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberRole.TEACHER);
    verify(classroomMemberRepository, never()).insert(any());
  }

  @Test
  @DisplayName("updates a classroom member role")
  void updateClassroomMemberRole_valid_returns200() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberRepository.update(any(ClassroomMember.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateClassroomMemberRoleCommand command =
        UpdateClassroomMemberRoleCommand.builder().role(ClassroomMemberRole.ADMIN).build();

    mockMvc
        .perform(
            patch("/admin/classrooms/{classroomId}/members/{memberId}", CLASSROOM_ID, MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$.role").value("ADMIN"));

    ArgumentCaptor<ClassroomMember> captor = ArgumentCaptor.forClass(ClassroomMember.class);
    verify(classroomMemberRepository).update(captor.capture());
    assertThat(captor.getValue().getRole())
        .isEqualTo(tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberRole.ADMIN);
  }

  @Test
  @DisplayName("removes a classroom member")
  void removeClassroomMember_valid_returns204() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberRepository.update(any(ClassroomMember.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            delete("/admin/classrooms/{classroomId}/members/{memberId}", CLASSROOM_ID, MEMBER_ID)
                .with(adminJwt()))
        .andExpect(status().isNoContent());

    ArgumentCaptor<ClassroomMember> captor = ArgumentCaptor.forClass(ClassroomMember.class);
    verify(classroomMemberRepository).update(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(ClassroomMemberStatus.REMOVED);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to update the role of a member that does not exist, PATCH, MISSING",
    "fails to update the role of a removed member, PATCH, REMOVED",
    "fails to remove a member that does not exist, DELETE, MISSING"
  })
  void memberMutation_failsWhenMemberUnavailable(
      String displayName, String httpMethod, String memberState) throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID))
        .thenReturn(
            switch (memberState) {
              case "MISSING" -> Optional.empty();
              case "REMOVED" ->
                  Optional.of(
                      classroomMember(
                          MEMBER_ID, CLASSROOM_ID, ACCOUNT_ID, ClassroomMemberStatus.REMOVED));
              default -> throw new IllegalArgumentException("Unsupported state: " + memberState);
            });

    var request =
        switch (httpMethod) {
          case "PATCH" ->
              patch("/admin/classrooms/{classroomId}/members/{memberId}", CLASSROOM_ID, MEMBER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      jsonMapper.writeValueAsString(
                          UpdateClassroomMemberRoleCommand.builder()
                              .role(ClassroomMemberRole.ADMIN)
                              .build()));
          case "DELETE" ->
              delete("/admin/classrooms/{classroomId}/members/{memberId}", CLASSROOM_ID, MEMBER_ID);
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request.with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_MEMBER_NOT_FOUND"));

    verify(classroomMemberRepository, never()).update(any());
  }

  @Test
  @DisplayName("queries classroom members")
  void getAllClassroomMembers_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomMemberRepository.query(any(BaseQuery.class)))
        .thenReturn(Stream.of(classroomMember()));

    ClassroomMemberFilter filter = ClassroomMemberFilter.builder().build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$[0].accountId").value(ACCOUNT_ID))
        .andExpect(jsonPath("$[0].email").value(MEMBER_EMAIL))
        .andExpect(jsonPath("$[0].name").value(MEMBER_NAME));

    ArgumentCaptor<BaseQuery> queryCaptor = ArgumentCaptor.forClass(BaseQuery.class);
    verify(classroomMemberRepository).query(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getFilters())
        .anyMatch(
            f ->
                "classroomId".equals(f.getField()) && CLASSROOM_ID.toString().equals(f.getValue()));
  }

  @Test
  @DisplayName("fails to query members when the classroom does not exist")
  void getAllClassroomMembers_classroomNotFound_returns404() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    ClassroomMemberFilter filter = ClassroomMemberFilter.builder().build();

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members/query", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"));

    verify(classroomMemberRepository, never()).query(any());
  }
}
