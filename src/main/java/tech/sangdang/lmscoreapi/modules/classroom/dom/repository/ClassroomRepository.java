package tech.sangdang.lmscoreapi.modules.classroom.dom.repository;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.classroom.dom.Classroom;

@Repository
public interface ClassroomRepository
    extends BaseCommandRepository<Classroom, UUID>, BaseQueryRepository<Classroom, UUID> {}
