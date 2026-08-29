package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMembersCommand;

public interface ClassroomMemberService {

  List<ClassroomMemberResponse> createClassroomMembers(
      UUID classroomId, CreateClassroomMembersCommand command);

  void removeClassroomMember(UUID classroomId, UUID memberId);

  /**
   * Returns every member of the classroom, not paginated.
   *
   * @param classroomId classroom whose members to load
   */
  List<ClassroomMemberResponse> getAllClassroomMembers(UUID classroomId);
}
