package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.util.Optional;
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
      WHERE session_id = :sessionId AND classroom_member_id = :classroomMemberId
      """)
  Optional<ClassroomSessionAttendance> findBySessionIdAndClassroomMemberId(
      @NonNull @Param("sessionId") UUID sessionId,
      @NonNull @Param("classroomMemberId") UUID classroomMemberId);
}
