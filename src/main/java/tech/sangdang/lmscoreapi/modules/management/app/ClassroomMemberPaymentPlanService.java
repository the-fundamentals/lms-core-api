package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberPaymentPlanResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberPaymentPlanCommand;

public interface ClassroomMemberPaymentPlanService {

  /**
   * Creates a current payment plan for the member.
   *
   * <ul>
   *   <li>Disables any previous current plan; this plan is current for following sessions.
   * </ul>
   *
   * @param classroomId classroom the member belongs to
   * @param memberId member receiving the plan
   * @param command type and amount; currency optional (defaults to VND)
   */
  ClassroomMemberPaymentPlanResponse createClassroomMemberPaymentPlan(
      UUID classroomId, UUID memberId, CreateClassroomMemberPaymentPlanCommand command);

  /**
   * Lists payment plans for the member.
   *
   * <ul>
   *   <li>Empty list means unpaid — that is valid.
   *   <li>Current plan first, then newest {@code createdDate}.
   * </ul>
   *
   * @param classroomId classroom the member belongs to
   * @param memberId member whose plans to load
   */
  List<ClassroomMemberPaymentPlanResponse> getClassroomMemberPaymentPlans(
      UUID classroomId, UUID memberId);

  /**
   * Lists each member's current payment plan for the classroom.
   *
   * <ul>
   *   <li>Empty list means no member has a current plan — that is valid.
   *   <li>Replaced plans and members with no current plan are omitted.
   * </ul>
   *
   * @param classroomId classroom whose members' current plans to load
   */
  List<ClassroomMemberPaymentPlanResponse> getAllClassroomPaymentPlans(UUID classroomId);

  /**
   * Deletes a payment plan.
   *
   * <ul>
   *   <li>Only within 5 minutes of creation (409 after that).
   *   <li>Does not restore a previous plan as current.
   * </ul>
   *
   * @param classroomId classroom the member belongs to
   * @param memberId member that owns the plan
   * @param paymentPlanId plan to delete
   */
  void deleteClassroomMemberPaymentPlan(UUID classroomId, UUID memberId, UUID paymentPlanId);
}
