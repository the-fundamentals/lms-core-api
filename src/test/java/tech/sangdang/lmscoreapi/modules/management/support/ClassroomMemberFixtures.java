package tech.sangdang.lmscoreapi.modules.management.support;

import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CREATED_AT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.MODIFIED_AT;

import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberRole;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberStatus;

public final class ClassroomMemberFixtures {

  public static final UUID MEMBER_ID = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
  public static final UUID SECOND_MEMBER_ID =
      UUID.fromString("1b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed");
  public static final String ACCOUNT_ID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
  public static final String SECOND_ACCOUNT_ID = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d";
  public static final UUID ACCOUNT_PROFILE_ID = UUID.fromString(ACCOUNT_ID);
  public static final UUID SECOND_ACCOUNT_PROFILE_ID = UUID.fromString(SECOND_ACCOUNT_ID);
  public static final String MEMBER_EMAIL = "alex@example.com";
  public static final String SECOND_MEMBER_EMAIL = "jordan@example.com";
  public static final String MEMBER_NAME = "Alex Nguyen";
  public static final String SECOND_MEMBER_NAME = "Jordan Lee";

  private ClassroomMemberFixtures() {}

  public static ClassroomMember classroomMember() {
    return classroomMember(MEMBER_ID, CLASSROOM_ID, ACCOUNT_ID, ClassroomMemberStatus.ACTIVE);
  }

  public static ClassroomMember classroomMember(
      UUID id, UUID classroomId, String accountId, ClassroomMemberStatus status) {
    return new ClassroomMember()
        .setId(id)
        .setClassroomId(classroomId)
        .setAccountId(accountId)
        .setRole(ClassroomMemberRole.STUDENT)
        .setStatus(status)
        .setEmail(MEMBER_EMAIL)
        .setName(MEMBER_NAME)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }
}
