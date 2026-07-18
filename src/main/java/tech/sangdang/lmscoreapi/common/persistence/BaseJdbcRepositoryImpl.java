package tech.sangdang.lmscoreapi.common.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.common.querying.QueryFilterConditions;

@Transactional(readOnly = true)
public class BaseJdbcRepositoryImpl<Entity, IdType>
    implements BaseCommandRepository<Entity, IdType>, BaseQueryRepository<Entity, IdType> {

  private final JdbcAggregateOperations operations;
  private final Class<Entity> entityClass;

  public BaseJdbcRepositoryImpl(
      @NonNull JdbcAggregateOperations operations,
      @NonNull PersistentEntity<Entity, ?> entity,
      @NonNull JdbcConverter converter) {
    this.operations = operations;
    this.entityClass = entity.getType();
  }

  @Transactional
  @Override
  public Entity insert(@NonNull Entity entity) {
    return operations.insert(entity);
  }

  @Transactional
  @Override
  public List<Entity> insertAll(@NonNull Iterable<@NonNull Entity> entities) {
    return toList(operations.insertAll(entities));
  }

  @Transactional
  @Override
  public Entity update(@NonNull Entity entity) {
    return operations.update(entity);
  }

  @Transactional
  @Override
  public List<Entity> updateAll(@NonNull Iterable<@NonNull Entity> entities) {
    return operations.updateAll(entities);
  }

  @Override
  public Optional<Entity> findById(@NonNull IdType id) {
    return Optional.ofNullable(operations.findById(id, entityClass));
  }

  @Override
  public boolean existsById(@NonNull IdType id) {
    return operations.existsById(id, entityClass);
  }

  @Override
  public Stream<Entity> query(@NonNull BaseQuery baseQuery) {
    return operations.streamAll(toRelationalQuery(baseQuery), entityClass);
  }

  private Query toRelationalQuery(BaseQuery baseQuery) {
    Criteria criteria = Criteria.empty();

    if (baseQuery.hasFilters()) {
      for (QueryFilterConditions filter : baseQuery.getFilters()) {
        criteria = criteria.and(toCriteria(filter));
      }
    }

    Query query = Query.query(criteria).limit(baseQuery.getSize()).offset(baseQuery.getOffset());

    if (baseQuery.hasSort()) {
      Sort.Direction direction =
          "desc".equalsIgnoreCase(baseQuery.getSortDirection())
              ? Sort.Direction.DESC
              : Sort.Direction.ASC;
      query = query.sort(Sort.by(direction, baseQuery.getSortBy()));
    }

    return query;
  }

  private Criteria toCriteria(QueryFilterConditions filter) {
    String field = filter.getField();
    String value = filter.getValue();

    return switch (filter.getOperator()) {
      case "eq" -> Criteria.where(field).is(value);
      case "like" -> Criteria.where(field).like("%" + value + "%");
      case "gt" -> Criteria.where(field).greaterThan(value);
      case "lt" -> Criteria.where(field).lessThan(value);
      case "gte" -> Criteria.where(field).greaterThanOrEquals(value);
      case "lte" -> Criteria.where(field).lessThanOrEquals(value);
      default -> throw new IllegalArgumentException("Unknown operator: " + filter.getOperator());
    };
  }

  private static <T> List<T> toList(Iterable<T> iterable) {
    if (iterable instanceof List<T> list) {
      return list;
    }
    List<T> result = new ArrayList<>();
    StreamSupport.stream(iterable.spliterator(), false).forEach(result::add);
    return result;
  }
}
