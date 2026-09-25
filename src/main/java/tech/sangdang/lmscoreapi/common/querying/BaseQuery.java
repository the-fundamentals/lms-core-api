package tech.sangdang.lmscoreapi.common.querying;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class BaseQuery {
  protected Integer page;
  protected Integer size;
  protected String sortBy;
  protected String sortDirection;
  protected List<QueryFilterConditions> filters;

  public BaseQuery() {}

  public int getOffset() {
    return page * size;
  }

  public boolean hasFilters() {
    return filters != null && !filters.isEmpty();
  }

  public boolean hasSort() {
    return sortBy != null;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final BaseQuery baseQuery;

    public Builder() {
      baseQuery = new BaseQuery();
    }

    public Builder fetchFirst() {
      baseQuery.setPage(0);
      baseQuery.setSize(1);
      return this;
    }

    /** This max fetches only 10,000 records */
    public Builder fetchAll() {
      baseQuery.setPage(0);
      baseQuery.setSize(10_000);
      return this;
    }

    public Builder pagination(Integer page, Integer size) {
      baseQuery.setPage(page);
      baseQuery.setSize(size);
      return this;
    }

    public Builder sort(String field, Sort.Direction direction) {
      baseQuery.setSortBy(field);
      baseQuery.setSortDirection(direction.name());
      return this;
    }

    public Builder addFilter(String field, String operator, String value) {
      if (baseQuery.getFilters() == null) {
        baseQuery.setFilters(new ArrayList<>());
      }

      baseQuery.getFilters().add(QueryFilterConditions.of(field, operator, value));
      return this;
    }

    public Builder addFilter(String field, String operator, Enum<?> value) {
      if (baseQuery.getFilters() == null) {
        baseQuery.setFilters(new ArrayList<>());
      }

      baseQuery.getFilters().add(QueryFilterConditions.of(field, operator, value.name()));
      return this;
    }

    public BaseQuery build() {
      return baseQuery;
    }
  }
}
