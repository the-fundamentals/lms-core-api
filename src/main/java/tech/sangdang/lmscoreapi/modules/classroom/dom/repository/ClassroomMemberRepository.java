package tech.sangdang.lmscoreapi.modules.classroom.dom.repository;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.classroom.dom.ClassroomMember;

public interface ClassroomMemberRepository
    extends BaseCommandRepository<ClassroomMember, UUID>,
        BaseQueryRepository<ClassroomMember, UUID> {

  @Query(
      """
      SELECT * FROM classroom_member
      WHERE classroom_id = :classroomId AND account_id = :accountId
      """)
  Optional<ClassroomMember> findByClassroomIdAndAccountId(
      @NonNull @Param("classroomId") UUID classroomId,
      @NonNull @Param("accountId") String accountId);
}
