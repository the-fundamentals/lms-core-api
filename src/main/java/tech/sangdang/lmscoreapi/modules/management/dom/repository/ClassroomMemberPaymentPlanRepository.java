package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberPaymentPlan;

@Repository
public interface ClassroomMemberPaymentPlanRepository
    extends BaseCommandRepository<ClassroomMemberPaymentPlan, UUID>,
        BaseQueryRepository<ClassroomMemberPaymentPlan, UUID> {

  /**
   * Returns the member's current payment plan, if they have one.
   *
   * @param classroomMemberId classroom member whose current plan to load
   */
  @Query(
      """
      SELECT * FROM classroom_member_payment_plan
      WHERE classroom_member_id = :classroomMemberId AND is_current = true
      """)
  Optional<ClassroomMemberPaymentPlan> findCurrent(
      @NonNull @Param("classroomMemberId") UUID classroomMemberId);

  /**
   * Returns every payment plan for the member. The current plan is first, then remaining plans by
   * {@code created_date} descending.
   *
   * @param classroomMemberId classroom member whose plans to load
   */
  @Query(
      """
      SELECT * FROM classroom_member_payment_plan
      WHERE classroom_member_id = :classroomMemberId
      ORDER BY is_current DESC, created_date DESC
      """)
  List<ClassroomMemberPaymentPlan> findByMember(
      @NonNull @Param("classroomMemberId") UUID classroomMemberId);
}
