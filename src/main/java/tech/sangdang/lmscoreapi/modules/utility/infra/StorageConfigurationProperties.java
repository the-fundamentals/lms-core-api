package tech.sangdang.lmscoreapi.modules.utility.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;
import tech.sangdang.lmscoreapi.common.CloudMode;

import java.util.Objects;

@ConfigurationProperties(prefix = "app.utility.storage")
public record StorageConfigurationProperties(
    String landingZoneBucketName,
    String publicStoreBucketName,
    String endpointOverride,
    String region,
    Boolean useDummyCredentials,
    Integer presignedExpirationMinutes,
    CloudMode cloudMode) {


  public StorageConfigurationProperties {
    cloudMode = Objects.requireNonNullElse(cloudMode, CloudMode.AWS);
  }
}
