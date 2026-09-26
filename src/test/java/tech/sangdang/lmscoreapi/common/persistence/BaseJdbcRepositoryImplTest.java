package tech.sangdang.lmscoreapi.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.relational.core.query.Query;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.common.querying.Operators;

@ExtendWith(MockitoExtension.class)
@DisplayName("Base JDBC repository")
class BaseJdbcRepositoryImplTest {

  @Mock private JdbcAggregateOperations operations;
  @Mock private PersistentEntity<SampleEntity, ?> persistentEntity;
  @Mock private JdbcConverter converter;

  private BaseJdbcRepositoryImpl<SampleEntity, Long> repository;

  @BeforeEach
  void setUp() {
    when(persistentEntity.getType()).thenReturn(SampleEntity.class);
    repository = new BaseJdbcRepositoryImpl<>(operations, persistentEntity, converter);
  }

  @Nested
  @DisplayName("command")
  class Command {

    @Test
    @DisplayName("inserts an entity")
    void insert_entity_delegatesToOperations() {
      SampleEntity entity = new SampleEntity(1L, "a");
      when(operations.insert(entity)).thenReturn(entity);

      assertThat(repository.insert(entity)).isSameAs(entity);
      verify(operations).insert(same(entity));
    }

    @Test
    @DisplayName("inserts all entities")
    void insertAll_entities_delegatesToOperations() {
      List<SampleEntity> entities = List.of(new SampleEntity(1L, "a"), new SampleEntity(2L, "b"));
      when(operations.insertAll(entities)).thenReturn(entities);

      assertThat(repository.insertAll(entities)).containsExactlyElementsOf(entities);
      verify(operations).insertAll(same(entities));
    }

    @Test
    @DisplayName("inserts all from a non-list iterable")
    void insertAll_nonListIterable_materializesList() {
      Set<SampleEntity> entities = Set.of(new SampleEntity(1L, "a"));
      when(operations.insertAll(entities)).thenReturn(entities);

      assertThat(repository.insertAll(entities)).containsExactlyElementsOf(entities);
    }

    @Test
    @DisplayName("updates an entity")
    void update_entity_delegatesToOperations() {
      SampleEntity entity = new SampleEntity(1L, "a");
      when(operations.update(entity)).thenReturn(entity);

      assertThat(repository.update(entity)).isSameAs(entity);
      verify(operations).update(same(entity));
    }

    @Test
    @DisplayName("updates all entities")
    void updateAll_entities_delegatesToOperations() {
      List<SampleEntity> entities = List.of(new SampleEntity(1L, "a"));
      when(operations.updateAll(entities)).thenReturn(entities);

      assertThat(repository.updateAll(entities)).containsExactlyElementsOf(entities);
      verify(operations).updateAll(same(entities));
    }

    @Test
    @DisplayName("deletes by id")
    void deleteById_id_delegatesToOperations() {
      repository.deleteById(9L);

      verify(operations).deleteById(9L, SampleEntity.class);
    }
  }

  @Nested
  @DisplayName("query")
  class QueryOps {

    @Test
    @DisplayName("finds by id")
    void findById_present_returnsOptional() {
      SampleEntity entity = new SampleEntity(1L, "a");
      when(operations.findById(1L, SampleEntity.class)).thenReturn(entity);

      assertThat(repository.findById(1L)).contains(entity);
    }

    @Test
    @DisplayName("returns empty when id is missing")
    void findById_missing_returnsEmpty() {
      when(operations.findById(1L, SampleEntity.class)).thenReturn(null);

      assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    @DisplayName("finds all by ids")
    void findAllById_ids_delegatesToOperations() {
      List<Long> ids = List.of(1L, 2L);
      List<SampleEntity> entities = List.of(new SampleEntity(1L, "a"));
      when(operations.findAllById(ids, SampleEntity.class)).thenReturn(entities);

      assertThat(repository.findAllById(ids)).containsExactlyElementsOf(entities);
    }

    @Test
    @DisplayName("checks existence by id")
    void existsById_id_delegatesToOperations() {
      when(operations.existsById(1L, SampleEntity.class)).thenReturn(true);

      assertThat(repository.existsById(1L)).isTrue();
    }

    @Test
    @DisplayName("streams with empty filters and pagination")
    void query_noFilters_appliesLimitAndOffset() {
      SampleEntity entity = new SampleEntity(1L, "a");
      when(operations.streamAll(any(Query.class), eq(SampleEntity.class)))
          .thenReturn(Stream.of(entity));

      BaseQuery baseQuery = BaseQuery.builder().pagination(2, 10).build();

      assertThat(repository.query(baseQuery)).containsExactly(entity);

      Query captured = captureStreamedQuery();
      assertThat(captured.getLimit()).isEqualTo(10);
      assertThat(captured.getOffset()).isEqualTo(20);
      // Criteria.empty() is still present on the Query; assert emptiness, not Optional.empty()
      assertThat(captured.getCriteria()).isPresent();
      assertThat(captured.getCriteria().orElseThrow().isEmpty()).isTrue();
      assertThat(captured.getSort()).isEqualTo(Sort.unsorted());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
      "eq, (name = 'alpha')",
      "like, (name LIKE '%alpha%')",
      "gt, (name > 'alpha')",
      "lt, (name < 'alpha')",
      "gte, (name >= 'alpha')",
      "lte, (name <= 'alpha')"
    })
    @DisplayName("maps filter operators to criteria")
    void query_operator_mapsToCriteria(String operator, String expectedCriteria) {
      when(operations.streamAll(any(Query.class), eq(SampleEntity.class)))
          .thenReturn(Stream.empty());

      BaseQuery baseQuery =
          BaseQuery.builder().pagination(0, 5).addFilter("name", operator, "alpha").build();

      repository.query(baseQuery).toList();

      Query captured = captureStreamedQuery();
      assertThat(captured.getCriteria()).isPresent();
      assertThat(captured.getCriteria().orElseThrow().toString()).isEqualTo(expectedCriteria);
    }

    @Test
    @DisplayName("ands multiple filters")
    void query_multipleFilters_andsCriteria() {
      when(operations.streamAll(any(Query.class), eq(SampleEntity.class)))
          .thenReturn(Stream.empty());

      BaseQuery baseQuery =
          BaseQuery.builder()
              .fetchFirst()
              .addFilter("name", Operators.EQUAL, "alpha")
              .addFilter("status", Operators.EQUAL, "ACTIVE")
              .build();

      repository.query(baseQuery).toList();

      Query captured = captureStreamedQuery();
      assertThat(captured.getCriteria().orElseThrow().toString())
          .isEqualTo("(name = 'alpha') AND (status = 'ACTIVE')");
    }

    @Test
    @DisplayName("applies ascending sort")
    void query_sortAsc_appliesSort() {
      when(operations.streamAll(any(Query.class), eq(SampleEntity.class)))
          .thenReturn(Stream.empty());

      BaseQuery baseQuery =
          BaseQuery.builder().pagination(0, 10).sort("name", Sort.Direction.ASC).build();

      repository.query(baseQuery).toList();

      assertThat(captureStreamedQuery().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    @DisplayName("applies descending sort case-insensitively")
    void query_sortDesc_appliesSort() {
      when(operations.streamAll(any(Query.class), eq(SampleEntity.class)))
          .thenReturn(Stream.empty());

      BaseQuery baseQuery = BaseQuery.builder().pagination(0, 10).build();
      baseQuery.setSortBy("name");
      baseQuery.setSortDirection("DeSc");

      repository.query(baseQuery).toList();

      assertThat(captureStreamedQuery().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "name"));
    }

    @Test
    @DisplayName("rejects an unknown filter operator")
    void query_unknownOperator_throws() {
      BaseQuery baseQuery =
          BaseQuery.builder().fetchFirst().addFilter("name", "bogus", "alpha").build();

      assertThatThrownBy(() -> repository.query(baseQuery))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Unknown operator: bogus");
    }
  }

  private Query captureStreamedQuery() {
    ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
    verify(operations).streamAll(captor.capture(), eq(SampleEntity.class));
    return captor.getValue();
  }

  /** Minimal stand-in entity for repository unit tests. */
  record SampleEntity(Long id, String name) {}
}
