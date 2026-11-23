package tech.sangdang.lmscoreapi.modules.utility.infra;

import java.net.URI;
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
import tech.sangdang.lmscoreapi.common.CloudMode;

@RequiredArgsConstructor
@Configuration
public class S3Config {

  private static final String LOCALSTACK_ENDPOINT = "http://localhost:4566";
  private static final Region DEFAULT_REGION = Region.AP_SOUTHEAST_1;

  private final StorageConfigurationProperties storage;

  @Bean
  @Qualifier("storage")
  public S3Client s3Client() {
    S3ClientBuilder builder = S3Client.builder().region(region());

    if (storage.cloudMode() == CloudMode.LOCALSTACK) {
      applyEmulator(builder);
    } else if (StringUtils.hasText(storage.endpointOverride())) {
      builder.endpointOverride(URI.create(storage.endpointOverride()));
    }

    return builder.build();
  }

  @Bean
  @Qualifier("storage")
  public S3Presigner s3Presigner(@Qualifier("storage") S3Client s3Client) {
    // Same client = same region/endpoint/creds; avoids duplicating LOCALSTACK vs AWS setup.
    return S3Presigner.builder().s3Client(s3Client).build();
  }

  private Region region() {
    return StringUtils.hasText(storage.region()) ? Region.of(storage.region()) : DEFAULT_REGION;
  }

  private void applyEmulator(S3ClientBuilder builder) {
    String endpoint =
        StringUtils.hasText(storage.endpointOverride())
            ? storage.endpointOverride()
            : LOCALSTACK_ENDPOINT;
    // Path-style: virtual-hosted http://bucket.localhost:4566 does not resolve on the host OS.
    builder
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
  }
}
