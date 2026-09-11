package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSchedule;

@Repository
public interface ClassroomScheduleRepository
    extends BaseQueryRepository<ClassroomSchedule, UUID>,
        BaseCommandRepository<ClassroomSchedule, UUID> {

  @Query(
      """
      SELECT * FROM classroom_schedule
      WHERE classroom_id = :classroomId AND deleted_date IS NULL
      """)
  List<ClassroomSchedule> findByClassroomIdAndDeletedDateIsNull(
      @NonNull @Param("classroomId") UUID classroomId);
}
