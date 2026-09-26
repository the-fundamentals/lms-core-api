package tech.sangdang.lmscoreapi.common.querying;

import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;

/**
 * Shared MapStruct parent for API filter → {@link BaseQuery} mapping.
 *
 * <p>Domain mappers extend this with their OpenAPI filter type so {@link #toBaseQuery} is
 * inherited instead of redeclared on every mapper.
 */
public interface QueryMapper<TFilter> {

  @BeanMapping(builder = @Builder(disableBuilder = true))
  BaseQuery toBaseQuery(TFilter apiFilter);
}
