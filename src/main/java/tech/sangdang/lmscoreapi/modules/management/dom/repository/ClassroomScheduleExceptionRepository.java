package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleException;

import java.util.UUID;

@Repository
public interface ClassroomScheduleExceptionRepository
    extends BaseCommandRepository<ClassroomScheduleException, UUID>,
        BaseQueryRepository<ClassroomScheduleException, UUID> {}
