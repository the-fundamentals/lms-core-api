package tech.sangdang.lmscoreapi.modules.management.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.helpers.SecurityTestSupport.adminJwt;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.classroom;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.ACCOUNT_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.ACCOUNT_PROFILE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_EMAIL;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_NAME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.SECOND_ACCOUNT_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.SECOND_ACCOUNT_PROFILE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.SECOND_MEMBER_EMAIL;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.SECOND_MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.SECOND_MEMBER_NAME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.classroomMember;

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
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberRole;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMembersCommand;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomMemberServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.internal.ClassroomRecordService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMemberMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomMemberController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomMemberServiceImpl.class,
  ClassroomRecordService.class,
  ClassroomMemberMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom member management")
class ClassroomMemberControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomMemberRepository classroomMemberRepository;
  @MockitoBean private AccountProfileRepository accountProfileRepository;

  @Test
  @DisplayName("creates a classroom member")
  void createClassroomMembers_valid_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(accountProfileRepository.findAllById(any())).thenReturn(List.of(accountProfile()));
    when(classroomMemberRepository.findByClassroomIdAndAccountIdIn(eq(CLASSROOM_ID), any()))
        .thenReturn(List.of());
    stubInsertAll();
    when(classroomRepository.update(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(membersCommand(memberItem())))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$[0].classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$[0].accountId").value(ACCOUNT_ID))
        .andExpect(jsonPath("$[0].role").value("STUDENT"))
        .andExpect(jsonPath("$[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$[0].email").value(MEMBER_EMAIL))
        .andExpect(jsonPath("$[0].name").value(MEMBER_NAME));

    ArgumentCaptor<Iterable<ClassroomMember>> captor = iterableCaptor();
    verify(classroomMemberRepository).insertAll(captor.capture());
    ClassroomMember inserted = only(captor.getValue());
    assertThat(inserted.getAccountId()).isEqualTo(ACCOUNT_ID);
    assertThat(inserted.getEmail()).isEqualTo(MEMBER_EMAIL);
    assertThat(inserted.getName()).isEqualTo(MEMBER_NAME);

    ArgumentCaptor<Classroom> classroomCaptor = ArgumentCaptor.forClass(Classroom.class);
    verify(classroomRepository).update(classroomCaptor.capture());
    assertThat(classroomCaptor.getValue().getNumberOfMembers()).isEqualTo(1);
  }

  @Test
  @DisplayName("creates multiple classroom members in one request")
  void createClassroomMembers_multiple_returns201() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(accountProfileRepository.findAllById(any()))
        .thenReturn(List.of(accountProfile(), secondAccountProfile()));
    when(classroomMemberRepository.findByClassroomIdAndAccountIdIn(eq(CLASSROOM_ID), any()))
        .thenReturn(List.of());
    stubInsertAll();
    when(classroomRepository.update(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        membersCommand(
                            memberItem(),
                            CreateClassroomMemberCommand.builder()
                                .accountId(SECOND_ACCOUNT_PROFILE_ID)
                                .role(ClassroomMemberRole.TEACHER)
                                .build())))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].accountId").value(ACCOUNT_ID))
        .andExpect(jsonPath("$[0].role").value("STUDENT"))
        .andExpect(jsonPath("$[1].accountId").value(SECOND_ACCOUNT_ID))
        .andExpect(jsonPath("$[1].role").value("TEACHER"));

    verify(accountProfileRepository, times(1)).findAllById(any());
    verify(accountProfileRepository, never()).findById(any());
    verify(classroomMemberRepository, times(1))
        .findByClassroomIdAndAccountIdIn(eq(CLASSROOM_ID), any());
    verify(classroomMemberRepository, times(1)).insertAll(any());
    verify(classroomMemberRepository, never()).insert(any());
    verify(classroomMemberRepository, never()).update(any());
    verify(classroomMemberRepository, never()).updateAll(any());

    ArgumentCaptor<Classroom> classroomCaptor = ArgumentCaptor.forClass(Classroom.class);
    verify(classroomRepository).update(classroomCaptor.capture());
    assertThat(classroomCaptor.getValue().getNumberOfMembers()).isEqualTo(2);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to create members when the classroom does not exist, STUDENT, 404, CLASSROOM_NOT_FOUND, false, false",
    "fails to create members when an account profile is missing, STUDENT, 404, ACCOUNT_PROFILE_NOT_FOUND, true, false"
  })
  void createClassroomMembers_fails(
      String displayName,
      ClassroomMemberRole role,
      int httpStatus,
      String errorCode,
      boolean classroomExists,
      boolean accountExists)
      throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID))
        .thenReturn(classroomExists ? Optional.of(classroom()) : Optional.empty());
    if (classroomExists) {
      when(accountProfileRepository.findAllById(any()))
          .thenReturn(accountExists ? List.of(accountProfile()) : List.of());
    }

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        membersCommand(
                            CreateClassroomMemberCommand.builder()
                                .accountId(ACCOUNT_PROFILE_ID)
                                .role(role)
                                .build())))
                .with(adminJwt()))
        .andExpect(status().is(httpStatus))
        .andExpect(jsonPath("$.code").value(errorCode));

    verify(classroomMemberRepository, never()).insertAll(any());
    verify(classroomMemberRepository, never()).updateAll(any());
    verify(classroomRepository, never()).update(any());
  }

  @Test
  @DisplayName("rejects duplicate account ids in one create request")
  void createClassroomMembers_duplicateAccountId_returns400() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        membersCommand(
                            memberItem(),
                            CreateClassroomMemberCommand.builder()
                                .accountId(ACCOUNT_PROFILE_ID)
                                .role(ClassroomMemberRole.TEACHER)
                                .build())))
                .with(adminJwt()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("DUPLICATE_ACCOUNT_ID"));

    verify(accountProfileRepository, never()).findAllById(any());
    verify(classroomMemberRepository, never()).insertAll(any());
    verify(classroomMemberRepository, never()).updateAll(any());
  }

  @Test
  @DisplayName("reactivates a previously removed classroom member")
  void createClassroomMembers_reactivatesRemoved_returns201() throws Exception {
    ClassroomMember removed =
        classroomMember(MEMBER_ID, CLASSROOM_ID, ACCOUNT_ID, ClassroomMemberStatus.REMOVED);

    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(accountProfileRepository.findAllById(any())).thenReturn(List.of(accountProfile()));
    when(classroomMemberRepository.findByClassroomIdAndAccountIdIn(eq(CLASSROOM_ID), any()))
        .thenReturn(List.of(removed));
    when(classroomMemberRepository.updateAll(any()))
        .thenAnswer(invocation -> toList(invocation.getArgument(0)));
    when(classroomRepository.update(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        membersCommand(
                            CreateClassroomMemberCommand.builder()
                                .accountId(ACCOUNT_PROFILE_ID)
                                .role(ClassroomMemberRole.TEACHER)
                                .build())))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$[0].id").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$[0].role").value("TEACHER"))
        .andExpect(jsonPath("$[0].status").value("ACTIVE"));

    ArgumentCaptor<Iterable<ClassroomMember>> captor = iterableCaptor();
    verify(classroomMemberRepository).updateAll(captor.capture());
    ClassroomMember updated = only(captor.getValue());
    assertThat(updated.getStatus()).isEqualTo(ClassroomMemberStatus.ACTIVE);
    assertThat(updated.getRole())
        .isEqualTo(tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberRole.TEACHER);
    verify(classroomMemberRepository, never()).insertAll(any());

    ArgumentCaptor<Classroom> classroomCaptor = ArgumentCaptor.forClass(Classroom.class);
    verify(classroomRepository).update(classroomCaptor.capture());
    assertThat(classroomCaptor.getValue().getNumberOfMembers()).isEqualTo(1);
  }

  @Test
  @DisplayName("updates the role of an already active classroom member")
  void createClassroomMembers_alreadyActive_updatesRole() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(accountProfileRepository.findAllById(any())).thenReturn(List.of(accountProfile()));
    when(classroomMemberRepository.findByClassroomIdAndAccountIdIn(eq(CLASSROOM_ID), any()))
        .thenReturn(List.of(classroomMember()));
    when(classroomMemberRepository.updateAll(any()))
        .thenAnswer(invocation -> toList(invocation.getArgument(0)));

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/members", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        membersCommand(
                            CreateClassroomMemberCommand.builder()
                                .accountId(ACCOUNT_PROFILE_ID)
                                .role(ClassroomMemberRole.TEACHER)
                                .build())))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$[0].id").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$[0].role").value("TEACHER"))
        .andExpect(jsonPath("$[0].status").value("ACTIVE"));

    ArgumentCaptor<Iterable<ClassroomMember>> captor = iterableCaptor();
    verify(classroomMemberRepository).updateAll(captor.capture());
    assertThat(only(captor.getValue()).getRole())
        .isEqualTo(tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberRole.TEACHER);
    verify(classroomMemberRepository, never()).insertAll(any());
    verify(classroomRepository, never()).update(any());
  }

  @Test
  @DisplayName("removes a classroom member")
  void removeClassroomMember_valid_returns204() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberRepository.update(any(ClassroomMember.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(classroomRepository.findById(CLASSROOM_ID))
        .thenReturn(Optional.of(classroom().setNumberOfMembers(1)));
    when(classroomRepository.update(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            delete("/admin/classrooms/{classroomId}/members/{memberId}", CLASSROOM_ID, MEMBER_ID)
                .with(adminJwt()))
        .andExpect(status().isNoContent());

    ArgumentCaptor<ClassroomMember> captor = ArgumentCaptor.forClass(ClassroomMember.class);
    verify(classroomMemberRepository).update(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(ClassroomMemberStatus.REMOVED);

    ArgumentCaptor<Classroom> classroomCaptor = ArgumentCaptor.forClass(Classroom.class);
    verify(classroomRepository).update(classroomCaptor.capture());
    assertThat(classroomCaptor.getValue().getNumberOfMembers()).isZero();
  }

  @Test
  @DisplayName("fails to remove a member that does not exist")
  void removeClassroomMember_missing_returns404() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            delete("/admin/classrooms/{classroomId}/members/{memberId}", CLASSROOM_ID, MEMBER_ID)
                .with(adminJwt()))
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

  private void stubInsertAll() {
    when(classroomMemberRepository.insertAll(any()))
        .thenAnswer(
            invocation -> {
              List<ClassroomMember> result = new ArrayList<>();
              for (ClassroomMember incoming : toList(invocation.getArgument(0))) {
                UUID id =
                    ACCOUNT_ID.equals(incoming.getAccountId()) ? MEMBER_ID : SECOND_MEMBER_ID;
                result.add(
                    classroomMember(
                            id,
                            incoming.getClassroomId(),
                            incoming.getAccountId(),
                            ClassroomMemberStatus.ACTIVE)
                        .setRole(incoming.getRole())
                        .setEmail(incoming.getEmail())
                        .setName(incoming.getName()));
              }
              return result;
            });
  }

  private static CreateClassroomMembersCommand membersCommand(
      CreateClassroomMemberCommand... items) {
    return CreateClassroomMembersCommand.builder().members(List.of(items)).build();
  }

  private static CreateClassroomMemberCommand memberItem() {
    return CreateClassroomMemberCommand.builder()
        .accountId(ACCOUNT_PROFILE_ID)
        .role(ClassroomMemberRole.STUDENT)
        .build();
  }

  private static AccountProfile accountProfile() {
    String[] nameParts = MEMBER_NAME.split(" ", 2);
    return new AccountProfile()
        .setId(ACCOUNT_PROFILE_ID)
        .setEmail(MEMBER_EMAIL)
        .setFirstName(nameParts[0])
        .setLastName(nameParts[1]);
  }

  private static AccountProfile secondAccountProfile() {
    String[] nameParts = SECOND_MEMBER_NAME.split(" ", 2);
    return new AccountProfile()
        .setId(SECOND_ACCOUNT_PROFILE_ID)
        .setEmail(SECOND_MEMBER_EMAIL)
        .setFirstName(nameParts[0])
        .setLastName(nameParts[1]);
  }

  @SuppressWarnings("unchecked")
  private static ArgumentCaptor<Iterable<ClassroomMember>> iterableCaptor() {
    return ArgumentCaptor.forClass(Iterable.class);
  }

  private static ClassroomMember only(Iterable<ClassroomMember> members) {
    List<ClassroomMember> list = toList(members);
    assertThat(list).hasSize(1);
    return list.getFirst();
  }

  private static List<ClassroomMember> toList(Iterable<ClassroomMember> members) {
    return StreamSupport.stream(members.spliterator(), false).toList();
  }
}
