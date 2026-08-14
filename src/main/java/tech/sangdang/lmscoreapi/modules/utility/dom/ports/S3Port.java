package tech.sangdang.lmscoreapi.modules.utility.dom.ports;

import java.time.Duration;
import org.jspecify.annotations.NonNull;

public interface S3Port {
  String getUploadPresignedUrl(
      @NonNull String targetBucket, @NonNull String objectKey, @NonNull Duration expiration);

  String getDownloadPresignedUrl(
      @NonNull String targetBucket, @NonNull String objectKey, @NonNull Duration expiration);

  boolean exists(String objectKey, @NonNull String bucket);

  void copy(
      @NonNull String sourceKey,
      @NonNull String sourceBucket,
      @NonNull String destinationKey,
      @NonNull String destinationBucket);

  void delete(@NonNull String objectKey, @NonNull String bucket);
}
