package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;

public interface ClassroomMemberRepository
    extends BaseCommandRepository<ClassroomMember, UUID>,
        BaseQueryRepository<ClassroomMember, UUID> {

  @Query(
      """
      SELECT * FROM classroom_member
      WHERE classroom_id = :classroomId AND account_id IN (:accountIds)
      """)
  List<ClassroomMember> findByClassroomIdAndAccountIdIn(
      @NonNull @Param("classroomId") UUID classroomId,
      @NonNull @Param("accountIds") Collection<String> accountIds);
}
