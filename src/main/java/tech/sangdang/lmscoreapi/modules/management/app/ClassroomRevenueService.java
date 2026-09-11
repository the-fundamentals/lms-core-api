package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomRevenueResponse;
import tech.sangdang.lmscoreapi.generated.model.GetClassroomRevenueQuery;

public interface ClassroomRevenueService {

  /**
   * Returns classroom revenue for sessions in {@code [from, to)}.
   *
   * <ul>
   *   <li>ATTENDED students with a current plan only; currency is always VND.
   *   <li>{@code from} must be before {@code to} (400 otherwise).
   * </ul>
   *
   * @param classroomId classroom to total
   * @param query inclusive from, exclusive to on sessionDate
   */
  ClassroomRevenueResponse getClassroomRevenue(UUID classroomId, GetClassroomRevenueQuery query);
}
