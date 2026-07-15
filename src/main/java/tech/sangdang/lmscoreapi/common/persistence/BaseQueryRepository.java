package tech.sangdang.lmscoreapi.common.persistence;

import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;

@NoRepositoryBean
public interface BaseQueryRepository<Entity, IdType> extends Repository<Entity, IdType> {
  Optional<Entity> findById(@NonNull IdType id);

  boolean existsById(@NonNull IdType id);

  Stream<Entity> query(@NonNull BaseQuery query);
}
