package tech.sangdang.lmscoreapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import tech.sangdang.lmscoreapi.common.persistence.BaseJdbcRepositoryImpl;

@Configuration
@EnableJdbcAuditing
@EnableJdbcRepositories(
    basePackages = "tech.sangdang.lmscoreapi",
    repositoryBaseClass = BaseJdbcRepositoryImpl.class)
public class JdbcConfig {}
