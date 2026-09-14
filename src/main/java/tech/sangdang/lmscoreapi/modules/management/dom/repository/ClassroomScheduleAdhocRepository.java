package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleAdhoc;

@Repository
public interface ClassroomScheduleAdhocRepository
    extends BaseQueryRepository<ClassroomScheduleAdhoc, UUID>,
        BaseCommandRepository<ClassroomScheduleAdhoc, UUID> {

  /**
   * Returns ad-hoc occurrences for the classroom. {@code startDate}/{@code endDate} bound {@code
   * date} inclusively; a null bound is open.
   */
  @Query(
      """
      SELECT * FROM classroom_schedule_adhoc
      WHERE classroom_id = :classroomId
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
      """)
  List<ClassroomScheduleAdhoc> findByClassroomId(
      @NonNull @Param("classroomId") UUID classroomId,
      @Nullable @Param("startDate") LocalDate startDate,
      @Nullable @Param("endDate") LocalDate endDate);
}
