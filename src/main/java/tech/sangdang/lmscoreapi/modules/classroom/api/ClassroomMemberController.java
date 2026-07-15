package tech.sangdang.lmscoreapi.modules.classroom.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomMembersApi;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberFilter;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomMemberRoleCommand;
import tech.sangdang.lmscoreapi.modules.classroom.app.ClassroomMemberService;

@RestController
@RequiredArgsConstructor
public class ClassroomMemberController implements ClassroomMembersApi {

  private final ClassroomMemberService classroomMemberService;

  @Override
  public ResponseEntity<?> createClassroomMember(
      @NonNull UUID classroomId,
      @NonNull CreateClassroomMemberCommand createClassroomMemberCommand) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            classroomMemberService.createClassroomMember(
                classroomId, createClassroomMemberCommand));
  }

  @Override
  public ResponseEntity<?> updateClassroomMemberRole(
      @NonNull UUID classroomId,
      @NonNull UUID memberId,
      @NonNull UpdateClassroomMemberRoleCommand updateClassroomMemberRoleCommand) {
    return ResponseEntity.ok(
        classroomMemberService.updateClassroomMemberRole(
            classroomId, memberId, updateClassroomMemberRoleCommand));
  }

  @Override
  public ResponseEntity<?> removeClassroomMember(
      @NonNull UUID classroomId, @NonNull UUID memberId) {
    classroomMemberService.removeClassroomMember(classroomId, memberId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<?> getAllClassroomMembers(
      @NonNull UUID classroomId, @NonNull ClassroomMemberFilter classroomMemberFilter) {
    return ResponseEntity.ok(
        classroomMemberService.queryClassroomMembers(classroomId, classroomMemberFilter));
  }
}
