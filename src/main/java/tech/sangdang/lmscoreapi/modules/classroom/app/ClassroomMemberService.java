package tech.sangdang.lmscoreapi.modules.classroom.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomMemberRoleCommand;

public interface ClassroomMemberService {

  ClassroomMemberResponse createClassroomMember(
      UUID classroomId, CreateClassroomMemberCommand command);

  ClassroomMemberResponse updateClassroomMemberRole(
      UUID classroomId, UUID memberId, UpdateClassroomMemberRoleCommand command);

  void removeClassroomMember(UUID classroomId, UUID memberId);

  List<ClassroomMemberResponse> queryClassroomMembers(
      UUID classroomId, ClassroomMemberFilter filter);
}
