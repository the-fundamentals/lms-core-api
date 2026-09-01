package tech.sangdang.lmscoreapi.modules.management.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomMemberPaymentPlansApi;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberPaymentPlanCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomMemberPaymentPlanService;

@RestController
@RequiredArgsConstructor
public class ClassroomMemberPaymentPlanController implements ClassroomMemberPaymentPlansApi {

  private final ClassroomMemberPaymentPlanService classroomMemberPaymentPlanService;

  @Override
  public ResponseEntity<?> createClassroomMemberPaymentPlan(
      @NonNull UUID classroomId,
      @NonNull UUID memberId,
      @NonNull CreateClassroomMemberPaymentPlanCommand createClassroomMemberPaymentPlanCommand) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            classroomMemberPaymentPlanService.createClassroomMemberPaymentPlan(
                classroomId, memberId, createClassroomMemberPaymentPlanCommand));
  }

  @Override
  public ResponseEntity<?> getClassroomMemberPaymentPlans(
      @NonNull UUID classroomId, @NonNull UUID memberId) {
    return ResponseEntity.ok(
        classroomMemberPaymentPlanService.getClassroomMemberPaymentPlans(classroomId, memberId));
  }

  @Override
  public ResponseEntity<?> getAllClassroomPaymentPlans(@NonNull UUID classroomId) {
    return ResponseEntity.ok(
        classroomMemberPaymentPlanService.getAllClassroomPaymentPlans(classroomId));
  }

  @Override
  public ResponseEntity<?> deleteClassroomMemberPaymentPlan(
      @NonNull UUID classroomId, @NonNull UUID memberId, @NonNull UUID paymentPlanId) {
    classroomMemberPaymentPlanService.deleteClassroomMemberPaymentPlan(
        classroomId, memberId, paymentPlanId);
    return ResponseEntity.noContent().build();
  }
}
