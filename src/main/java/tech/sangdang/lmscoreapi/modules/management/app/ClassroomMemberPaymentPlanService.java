package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberPaymentPlanResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberPaymentPlanCommand;
import tech.sangdang.lmscoreapi.modules.management.dom.PaymentPlanType;

public interface ClassroomMemberPaymentPlanService {

  /**
   * Creates a current payment plan for the member.
   *
   * <ul>
   *   <li>Takes effect immediately; closes any previous current plan.
   *   <li>Does not support future-dated plans.
   *   <li>Only an ACTIVE STUDENT; other roles and removed members are 400.
   *   <li>The new plan is individual and is not rewritten when the classroom base changes.
   * </ul>
   *
   * @param classroomId classroom the member belongs to
   * @param memberId member receiving the plan
   * @param command type and amount
   */
  ClassroomMemberPaymentPlanResponse createClassroomMemberPaymentPlan(
      UUID classroomId, UUID memberId, CreateClassroomMemberPaymentPlanCommand command);

  /**
   * Replaces the current plan of each active student still on the classroom base.
   *
   * <ul>
   *   <li>Closes the open plan and inserts the next one at the same time.
   *   <li>The new plan stays on the classroom base.
   *   <li>Individual plans, teachers, and removed members are not loaded.
   * </ul>
   *
   * @param classroomId classroom whose base followers to rewrite
   * @param type new base type
   * @param amount new base amount
   */
  void applyClassroomBaseToActiveStudents(UUID classroomId, PaymentPlanType type, long amount);

  /**
   * Lists payment plans for the member.
   *
   * <ul>
   *   <li>Empty list means unpaid — that is valid.
   *   <li>Current plan first ({@code effectiveTo} null), then newest {@code effectiveFrom}.
   * </ul>
   *
   * @param classroomId classroom the member belongs to
   * @param memberId member whose plans to load
   */
  List<ClassroomMemberPaymentPlanResponse> getClassroomMemberPaymentPlans(
      UUID classroomId, UUID memberId);

  /**
   * Returns a payment plan by id for the member.
   *
   * @param classroomId classroom the member belongs to
   * @param memberId member that owns the plan
   * @param paymentPlanId plan to load
   */
  ClassroomMemberPaymentPlanResponse getClassroomMemberPaymentPlanById(
      UUID classroomId, UUID memberId, UUID paymentPlanId);

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
}
