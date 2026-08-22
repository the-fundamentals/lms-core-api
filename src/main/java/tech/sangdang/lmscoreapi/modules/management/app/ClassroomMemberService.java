package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMembersCommand;

public interface ClassroomMemberService {

  List<ClassroomMemberResponse> createClassroomMembers(
      UUID classroomId, CreateClassroomMembersCommand command);

  void removeClassroomMember(UUID classroomId, UUID memberId);

  List<ClassroomMemberResponse> queryClassroomMembers(
      UUID classroomId, ClassroomMemberFilter filter);
}
