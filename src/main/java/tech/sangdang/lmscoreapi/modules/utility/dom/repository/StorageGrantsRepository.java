package tech.sangdang.lmscoreapi.modules.utility.dom.repository;

import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.utility.dom.StorageGrants;

@Repository
public interface StorageGrantsRepository
    extends BaseQueryRepository<StorageGrants, Long>, BaseCommandRepository<StorageGrants, Long> {}
