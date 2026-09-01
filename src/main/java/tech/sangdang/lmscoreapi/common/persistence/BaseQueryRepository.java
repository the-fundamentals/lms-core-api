package tech.sangdang.lmscoreapi.common.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;

@NoRepositoryBean
public interface BaseQueryRepository<Entity, IdType> extends Repository<Entity, IdType> {
  Optional<Entity> findById(@NonNull IdType id);

  List<Entity> findAllById(@NonNull Iterable<@NonNull IdType> ids);

  boolean existsById(@NonNull IdType id);

  Stream<Entity> query(@NonNull BaseQuery query);
}
