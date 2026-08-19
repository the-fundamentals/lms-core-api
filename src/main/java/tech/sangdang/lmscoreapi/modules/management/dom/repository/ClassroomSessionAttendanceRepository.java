package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendance;

@Repository
public interface ClassroomSessionAttendanceRepository
    extends BaseCommandRepository<ClassroomSessionAttendance, UUID>,
        BaseQueryRepository<ClassroomSessionAttendance, UUID> {

  @Query(
      """
      SELECT * FROM classroom_attendance
      WHERE session_id = :sessionId AND classroom_member_id IN (:classroomMemberIds)
      """)
  List<ClassroomSessionAttendance> findBySessionIdAndClassroomMemberIdIn(
      @NonNull @Param("sessionId") UUID sessionId,
      @NonNull @Param("classroomMemberIds") Collection<UUID> classroomMemberIds);
}
