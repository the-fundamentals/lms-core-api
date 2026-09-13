package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleException;

@Repository
public interface ClassroomScheduleExceptionRepository
    extends BaseCommandRepository<ClassroomScheduleException, UUID>,
        BaseQueryRepository<ClassroomScheduleException, UUID> {}
