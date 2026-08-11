package tech.sangdang.lmscoreapi.modules.utility.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@RequiredArgsConstructor
@Configuration
public class S3Config {

  private final StorageConfigurationProperties storageConfigurationProperties;
  private static final String LOCALSTACK_ENDPOINT = "http://localhost:4566";

  @Bean
  @Qualifier("storage")
  public S3Client s3Client() {
    S3ClientBuilder builder = S3Client.builder();

    builder.region(
        StringUtils.hasText(storageConfigurationProperties.region())
            ? Region.of(storageConfigurationProperties.region())
            : Region.AP_SOUTHEAST_1);

    switch (storageConfigurationProperties.cloudMode()) {
      case LOCALSTACK ->
          builder
              .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
              .credentialsProvider(
                  StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")))
              /*
               This is needed in the localstack environment so the OS knows where localstack is.
               - If it's off, the SDK uses a virtual hosted URL: https://[bucket].localhost:4566 -> OS cannot route this
               - If it's on, the SDK uses a path style URL: https://localhost:4566
              */
              .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
      case AWS -> {
        if (StringUtils.hasText(storageConfigurationProperties.endpointOverride())) {
          builder.endpointOverride(URI.create(storageConfigurationProperties.endpointOverride()));
        }
      }
    }

    return builder.build();
  }

  @Bean
  @Qualifier("storage")
  public S3Presigner s3Presigner(@Qualifier("storage") S3Client s3Client) {
    S3Presigner.Builder builder = S3Presigner.builder().s3Client(s3Client);

    builder.region(
        StringUtils.hasText(storageConfigurationProperties.region())
            ? Region.of(storageConfigurationProperties.region())
            : Region.AP_SOUTHEAST_1);

    switch (storageConfigurationProperties.cloudMode()) {
      case LOCALSTACK ->
          builder
              .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
              .credentialsProvider(
                  StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")))
              /*
               This is needed in the localstack environment so the OS knows where localstack is.
               - If it's off, the SDK uses a virtual hosted URL: https://[bucket].localhost:4566 -> OS cannot route this
               - If it's on, the SDK uses a path style URL: https://localhost:4566
              */
              .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
      case AWS -> {
        if (StringUtils.hasText(storageConfigurationProperties.endpointOverride())) {
          builder.endpointOverride(URI.create(storageConfigurationProperties.endpointOverride()));
        }
      }
    }

    return builder.build();
  }
}
