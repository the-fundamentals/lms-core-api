package tech.sangdang.lmscoreapi.modules.utility.infra;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import tech.sangdang.lmscoreapi.modules.utility.dom.ports.S3Port;

@Slf4j
@Component
public class S3PortImpl implements S3Port {
  private final S3Presigner s3Presigner;
  private final S3Client s3Client;

  public S3PortImpl(
      @Qualifier("storage") S3Presigner s3Presigner, @Qualifier("storage") S3Client s3Client) {
    this.s3Presigner = s3Presigner;
    this.s3Client = s3Client;
  }

  @Override
  public String getUploadPresignedUrl(
      @NonNull String targetBucket, @NonNull String objectKey, @NonNull Duration expiration) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder().bucket(targetBucket).key(objectKey).build();

    PutObjectPresignRequest request =
        PutObjectPresignRequest.builder()
            .putObjectRequest(putObjectRequest)
            .signatureDuration(expiration)
            .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(request);

    return presignedRequest.url().toExternalForm();
  }

  @Override
  public String getDownloadPresignedUrl(
      @NonNull String targetBucket, @NonNull String objectKey, @NonNull Duration expiration) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().key(objectKey).bucket(targetBucket).build();

    GetObjectPresignRequest request =
        GetObjectPresignRequest.builder()
            .getObjectRequest(getObjectRequest)
            .signatureDuration(expiration)
            .build();

    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(request);
    return presignedRequest.url().toExternalForm();
  }

  @Override
  public boolean exists(String objectKey, @NonNull String bucket) {
    HeadObjectRequest headObjectRequest =
        HeadObjectRequest.builder().key(objectKey).bucket(bucket).build();

    log.info("Exist (objectKey: {}, bucket: {})", objectKey, bucket);

    try {
      s3Client.headObject(headObjectRequest);
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }

  @Override
  public void copy(
      @NonNull String sourceKey,
      @NonNull String sourceBucket,
      @NonNull String destinationKey,
      @NonNull String destinationBucket) {
    CopyObjectRequest copyObjectRequest =
        CopyObjectRequest.builder()
            .sourceKey(sourceKey)
            .sourceBucket(sourceBucket)
            .destinationKey(destinationKey)
            .destinationBucket(destinationBucket)
            .build();

    s3Client.copyObject(copyObjectRequest);
  }

  @Override
  public void delete(@NonNull String objectKey, @NonNull String bucket) {
    DeleteObjectRequest deleteObjectRequest =
        DeleteObjectRequest.builder().key(objectKey).bucket(bucket).build();

    s3Client.deleteObject(deleteObjectRequest);
  }
}
