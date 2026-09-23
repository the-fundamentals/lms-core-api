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
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleRecurrence;

@Repository
public interface ClassroomScheduleRecurrenceRepository
    extends BaseQueryRepository<ClassroomScheduleRecurrence, UUID>,
        BaseCommandRepository<ClassroomScheduleRecurrence, UUID> {

  @Query(
      """
      SELECT * FROM classroom_schedule_recurrence
      WHERE classroom_id = :classroomId AND deleted_date IS NULL
      """)
  List<ClassroomScheduleRecurrence> findByClassroomIdAndDeletedDateIsNull(
      @NonNull @Param("classroomId") UUID classroomId);

  //  @Query(
  //      """
  //              SELECT * FROM classroom_schedule_recurrence
  //              WHERE classroom_id IN :classroomIds AND deleted_date IS NULL
  //                  AND recurrence_start_date <= :currentDate AND (recur_until IS NULL OR
  // recur_until >= :currentDate)
  //              """)
  //  List<ClassroomScheduleRecurrence> findActiveByClassroomIdsAsOfDate(
  //      @NonNull @Param("classroomIds") List<UUID> classroomIds,
  //      @NonNull @Param("currentDate") LocalDate currentDate);

  /** Non-deleted recurrences for the classrooms that overlap {@code [startDate, endDate]}. */
  @Query(
      """
                  SELECT * FROM classroom_schedule_recurrence
                  WHERE classroom_id = ANY(:classroomIds) AND deleted_date IS NULL
                      AND recurrence_start_date <= :endDate
                      AND (recur_until IS NULL OR recur_until >= :startDate)
                  """)
  List<ClassroomScheduleRecurrence> findActiveByClassroomIdsAsOfDate(
      @NonNull @Param("classroomIds") UUID[] classroomIds,
      @NonNull @Param("startDate") LocalDate startDate,
      @NonNull @Param("endDate") LocalDate endDate);
}
