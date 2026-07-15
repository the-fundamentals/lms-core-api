package tech.sangdang.lmscoreapi.common.querying;

import java.util.List;
import lombok.Data;

@Data
public class BaseQuery {
  protected Integer page;
  protected Integer size;
  protected String sortBy;
  protected String sortDirection;
  protected List<QueryFilterConditions> filters;

  public int getOffset() {
    return page * size;
  }

  public boolean hasFilters() {
    return filters != null && !filters.isEmpty();
  }

  public boolean hasSort() {
    return sortBy != null;
  }
}
