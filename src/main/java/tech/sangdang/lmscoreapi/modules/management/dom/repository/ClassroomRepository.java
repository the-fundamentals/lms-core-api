package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomStatus;

@Repository
public interface ClassroomRepository
    extends BaseCommandRepository<Classroom, UUID>, BaseQueryRepository<Classroom, UUID> {

  List<Classroom> findByStatusIn(List<ClassroomStatus> statuses);
}
