package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMembersCommand;

public interface ClassroomMemberService {

  /**
   * Adds members to the classroom (or reactivates removed ones).
   *
   * <ul>
   *   <li>Duplicate account ids in the request are 400.
   *   <li>Existing REMOVED rows are set ACTIVE; name/email come from the account profile.
   *   <li>Classroom {@code numberOfMembers} only increases for new or newly reactivated members.
   * </ul>
   *
   * @param classroomId classroom to add members to
   * @param command members with account id and role
   * @return members in request order
   */
  List<ClassroomMemberResponse> createClassroomMembers(
      UUID classroomId, CreateClassroomMembersCommand command);

  /**
   * Marks a member REMOVED.
   *
   * <ul>
   *   <li>Already-removed members look like not found.
   *   <li>Decrements classroom {@code numberOfMembers}; does not delete the row.
   * </ul>
   *
   * @param classroomId classroom the member belongs to
   * @param memberId member to remove
   */
  void removeClassroomMember(UUID classroomId, UUID memberId);

  /**
   * Returns every member of the classroom, not paginated.
   *
   * <ul>
   *   <li>Includes REMOVED members.
   * </ul>
   *
   * @param classroomId classroom whose members to load
   * @return members
   */
  List<ClassroomMemberResponse> getAllClassroomMembers(UUID classroomId);
}
