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
   * <ul>
   *   <li>Current means {@code effective_to} is null.
   * </ul>
   *
   * @param classroomMemberId classroom member whose current plan to load
   */
  @Query(
      """
      SELECT * FROM classroom_member_payment_plan
      WHERE classroom_member_id = :classroomMemberId AND effective_to IS NULL
      """)
  Optional<ClassroomMemberPaymentPlan> findCurrent(
      @NonNull @Param("classroomMemberId") UUID classroomMemberId);

  /**
   * Returns every payment plan for the member. The current plan is first, then remaining plans by
   * {@code effective_from} descending.
   *
   * @param classroomMemberId classroom member whose plans to load
   */
  @Query(
      """
      SELECT * FROM classroom_member_payment_plan
      WHERE classroom_member_id = :classroomMemberId
      ORDER BY effective_to NULLS FIRST, effective_from DESC
      """)
  List<ClassroomMemberPaymentPlan> findByMember(
      @NonNull @Param("classroomMemberId") UUID classroomMemberId);

  /**
   * Returns each member's current payment plan for the classroom.
   *
   * <ul>
   *   <li>Inner join so members with no current plan are omitted (not a null-id row).
   *   <li>Only open-ended plans ({@code effective_to} null). Newest {@code effective_from} first.
   * </ul>
   *
   * @param classroomId classroom whose members' current plans to load
   */
  @Query(
      """
      SELECT p.*
      FROM classroom_member_payment_plan p
      INNER JOIN classroom_member m
        ON m.id = p.classroom_member_id
      WHERE m.classroom_id = :classroomId AND p.effective_to IS NULL
      ORDER BY p.effective_from DESC
      """)
  List<ClassroomMemberPaymentPlan> findByClassroom(@NonNull @Param("classroomId") UUID classroomId);
}
