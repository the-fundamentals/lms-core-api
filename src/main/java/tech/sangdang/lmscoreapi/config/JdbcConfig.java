package tech.sangdang.lmscoreapi.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import tech.sangdang.lmscoreapi.common.persistence.BaseJdbcRepositoryImpl;
import tech.sangdang.lmscoreapi.common.persistence.CreatorDetailsConverters;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableJdbcAuditing
@EnableJdbcRepositories(
    basePackages = "tech.sangdang.lmscoreapi",
    repositoryBaseClass = BaseJdbcRepositoryImpl.class)
@RequiredArgsConstructor
public class JdbcConfig extends AbstractJdbcConfiguration {

  private final JsonMapper jsonMapper;

  @Override
  protected List<?> userConverters() {
    return List.of(
        new CreatorDetailsConverters.ToJsonb(jsonMapper),
        new CreatorDetailsConverters.FromString(jsonMapper),
        new CreatorDetailsConverters.FromPGobject(jsonMapper));
  }
}
