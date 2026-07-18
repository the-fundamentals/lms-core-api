package tech.sangdang.lmscoreapi.common.persistence;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface BaseCommandRepository<Entity, IdType> extends Repository<Entity, IdType> {
  Entity insert(@NonNull Entity entity);

  List<Entity> insertAll(@NonNull Iterable<@NonNull Entity> entities);

  Entity update(@NonNull Entity entity);

  List<Entity> updateAll(@NonNull Iterable<@NonNull Entity> entities);
}
