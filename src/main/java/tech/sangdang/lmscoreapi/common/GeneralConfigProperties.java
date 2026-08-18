package tech.sangdang.lmscoreapi.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config")
public record GeneralConfigProperties(
        Boolean loadDummyAccountsIntoDatabase
) {}
