package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.time.LocalDateTime;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;

@Repository
public interface ClassroomSessionRepository
    extends BaseCommandRepository<ClassroomSession, UUID>,
        BaseQueryRepository<ClassroomSession, UUID> {

  /**
   * Sums current student payment-plan amounts for ATTENDED rows on sessions in {@code [from, to)}.
   *
   * <ul>
   *   <li>Uses each member's current plan only, not the plan at session time.
   *   <li>Teachers, ABSENT, UNSET, and members with no current plan are omitted.
   *   <li>Zero when nothing matches.
   * </ul>
   *
   * @param classroomId classroom whose sessions to include
   * @param from inclusive sessionDate start
   * @param to exclusive sessionDate end
   */
  @Query(
      """
      SELECT COALESCE(SUM(p.amount), 0)
      FROM classroom_session s
      JOIN classroom_attendance a ON a.session_id = s.id AND a.status = 'ATTENDED'
      JOIN classroom_member m ON m.id = a.classroom_member_id AND m.role = 'STUDENT'
      JOIN classroom_member_payment_plan p
        ON p.classroom_member_id = m.id AND p.is_current = true
      WHERE s.classroom_id = :classroomId
        AND s.session_date >= :from
        AND s.session_date < :to
      """)
  long sumRevenue(
      @NonNull @Param("classroomId") UUID classroomId,
      @NonNull @Param("from") LocalDateTime from,
      @NonNull @Param("to") LocalDateTime to);
}
