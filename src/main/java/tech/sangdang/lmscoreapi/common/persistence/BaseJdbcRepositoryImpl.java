package tech.sangdang.lmscoreapi.common.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.common.querying.Operators;
import tech.sangdang.lmscoreapi.common.querying.QueryFilterConditions;

@Slf4j
@Transactional(readOnly = true)
public class BaseJdbcRepositoryImpl<Entity, IdType>
    implements BaseCommandRepository<Entity, IdType>, BaseQueryRepository<Entity, IdType> {

  // JDBC write converters target Timestamp, not LocalDate — they cannot parse API filter strings.
  private static final ConversionService FILTER_VALUES =
      ApplicationConversionService.getSharedInstance();

  private final JdbcAggregateOperations operations;
  private final PersistentEntity<Entity, ?> entity;
  private final Class<Entity> entityClass;

  public BaseJdbcRepositoryImpl(
      @NonNull JdbcAggregateOperations operations,
      @NonNull PersistentEntity<Entity, ?> entity,
      @NonNull JdbcConverter converter) {
    this.operations = operations;
    this.entity = entity;
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

  @Transactional
  @Override
  public void deleteById(@NonNull IdType id) {
    operations.deleteById(id, entityClass);
  }

  @Override
  public Optional<Entity> findById(@NonNull IdType id) {
    return Optional.ofNullable(operations.findById(id, entityClass));
  }

  @Override
  public List<Entity> findAllById(@NonNull Iterable<@NonNull IdType> ids) {
    return toList(operations.findAllById(ids, entityClass));
  }

  @Override
  public boolean existsById(@NonNull IdType id) {
    return operations.existsById(id, entityClass);
  }

  @Override
  public Stream<Entity> query(@NonNull BaseQuery baseQuery) {
    var query = toRelationalQuery(baseQuery);
    log.debug("Executing query {}", query.toString());
    return operations.streamAll(query, entityClass);
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
    Object value =
        switch (filter.getOperator()) {
          case Operators.LIKE -> filter.getValue();
          case Operators.EQUAL,
              Operators.GREATER_THAN,
              Operators.LESS_THAN,
              Operators.GREATER_OR_EQUAL,
              Operators.LESS_OR_EQUAL ->
              coerceFilterValue(filter.getField(), filter.getValue());
          case Operators.IN -> coerceInFilterValue(filter);
          default ->
              throw new IllegalArgumentException("Unknown operator: " + filter.getOperator());
        };

    return switch (filter.getOperator()) {
      case Operators.EQUAL -> Criteria.where(field).is(value);
      case Operators.LIKE -> Criteria.where(field).like("%" + value + "%");
      case Operators.GREATER_THAN -> Criteria.where(field).greaterThan(value);
      case Operators.LESS_THAN -> Criteria.where(field).lessThan(value);
      case Operators.GREATER_OR_EQUAL -> Criteria.where(field).greaterThanOrEquals(value);
      case Operators.LESS_OR_EQUAL -> Criteria.where(field).lessThanOrEquals(value);
      case Operators.IN -> Criteria.where(field).in((Collection<?>) value);
      default -> throw new IllegalArgumentException("Unknown operator: " + filter.getOperator());
    };
  }

  private Collection<?> coerceInFilterValue(QueryFilterConditions filter) {
    Object raw = filter.getValue();
    if (!(raw instanceof Collection<?> values) || values.isEmpty()) {
      throw GenericBadRequestException.of(
          "INVALID_FILTER_VALUE",
          "IN filter requires a non-empty collection for field: " + filter.getField());
    }

    List<Object> coerced = new ArrayList<>(values.size());
    for (Object element : values) {
      coerced.add(coerceFilterValue(filter.getField(), element));
    }
    return coerced;
  }

  private Object coerceFilterValue(String field, Object value) {
    PersistentProperty<?> property = entity.getPersistentProperty(field);
    if (property == null) {
      throw GenericBadRequestException.of("UNKNOWN_FILTER_FIELD", "Unknown filter field: " + field);
    }

    Class<?> type = property.getType();
    if (value == null) {
      throw GenericBadRequestException.of(
          "INVALID_FILTER_VALUE", "Invalid filter value for field: " + field);
    }
    if (type.isInstance(value)) {
      return value;
    }
    if (type == String.class) {
      return value.toString();
    }

    try {
      Object converted = FILTER_VALUES.convert(value, type);
      if (converted == null) {
        throw GenericBadRequestException.of(
            "INVALID_FILTER_VALUE", "Invalid filter value for field: " + field);
      }
      return converted;
    } catch (ConversionException ex) {
      throw GenericBadRequestException.of(
          "INVALID_FILTER_VALUE", "Invalid filter value for field: " + field);
    }
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
