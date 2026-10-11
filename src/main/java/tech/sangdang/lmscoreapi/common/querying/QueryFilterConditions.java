package tech.sangdang.lmscoreapi.common.querying;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryFilterConditions {
  protected String field;
  protected String operator;
  /** Scalar (usually {@link String} from API) or {@link java.util.Collection} for {@code in}. */
  protected Object value;

  public static QueryFilterConditions of(String field, String operator, Object value) {
    return new QueryFilterConditions(field, operator, value);
  }
}
