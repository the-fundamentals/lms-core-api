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
  protected String value;

  public static QueryFilterConditions of(String field, String operator, String value) {
    return new QueryFilterConditions(field, operator, value);
  }
}
