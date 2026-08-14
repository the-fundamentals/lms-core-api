package tech.sangdang.lmscoreapi.modules.utility.app.impl;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.UploadToStorageCommand;
import tech.sangdang.lmscoreapi.generated.model.UploadToStorageResponse;
import tech.sangdang.lmscoreapi.modules.utility.app.StorageService;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPrivateCommand;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;
import tech.sangdang.lmscoreapi.modules.utility.dom.StorageGrants;
import tech.sangdang.lmscoreapi.modules.utility.dom.ports.S3Port;
import tech.sangdang.lmscoreapi.modules.utility.dom.repository.StorageGrantsRepository;
import tech.sangdang.lmscoreapi.modules.utility.infra.StorageConfigurationProperties;

@RequiredArgsConstructor
@Slf4j
@Service
public class StorageServiceImpl implements StorageService {
  private final S3Port s3Port;
  private final StorageConfigurationProperties configurationProperties;
  private final StorageGrantsRepository storageGrantsRepository;

  private static final String PUBLIC_KEY_PREFIX = "public/";
  private static final String PRIVATE_KEY_PREFIX = "private/";

  @Override
  public UploadToStorageResponse getPresignedUploadUrl(
      @Nullable UploadToStorageCommand uploadToStorageCommand) {
    String objectKey =
        resolveObjectAccess(
            UUID.randomUUID().toString(),
            uploadToStorageCommand != null && uploadToStorageCommand.getIsPublic());

    String uploadUrl =
        s3Port.getUploadPresignedUrl(
            configurationProperties.landingZoneBucketName(), objectKey, Duration.ofMinutes(5));

    String downloadUrl =
        s3Port.getDownloadPresignedUrl(
            configurationProperties.landingZoneBucketName(), objectKey, Duration.ofMinutes(15));

    return UploadToStorageResponse.builder()
        .objectKey(objectKey)
        .downloadUrl(downloadUrl)
        .uploadUrl(uploadUrl)
        .build();
  }

  @Override
  public void confirmPublicFileUpload(ConfirmUploadPublicCommand command) {
    if (!validatePublicKey(command.objectKey())) {
      throw new GenericBadRequestException("BAD_KEY", "Invalid public key");
    }

    if (s3Port.exists(command.objectKey(), configurationProperties.publicStoreBucketName())) {
      return;
    }

    if (!s3Port.exists(command.objectKey(), configurationProperties.landingZoneBucketName())) {
      throw new ObjectNotFoundException("StorageObject", command.objectKey());
    }

    s3Port.copy(
        command.objectKey(),
        configurationProperties.landingZoneBucketName(),
        command.objectKey(),
        configurationProperties.publicStoreBucketName());

    try {
      s3Port.delete(command.objectKey(), configurationProperties.landingZoneBucketName());
    } catch (Exception e) {
      log.warn(
          "Failed to delete object {} from bucket {}",
          command.objectKey(),
          configurationProperties.landingZoneBucketName());
    }
  }

  @Override
  public boolean validatePublicKey(String key) {
    return key.startsWith(PUBLIC_KEY_PREFIX);
  }

//  @Override
//  public StorageGrants confirmPrivateFileUpload(ConfirmUploadPrivateCommand command) {
//    if (!validatePrivateKey(command.objectKey())) {
//      throw new GenericBadRequestException("BAD_KEY", "Invalid private key");
//    }
//
//    if (!s3Port.exists(command.objectKey(), configurationProperties.landingZoneBucketName())) {
//      throw new ObjectNotFoundException("StorageObject", command.objectKey());
//    }
//
//    s3Port.copy(
//        command.objectKey(),
//        configurationProperties.landingZoneBucketName(),
//        command.objectKey(),
//        configurationProperties.publicStoreBucketName());
//
//    StorageGrants storageGrant =
//        new StorageGrants()
//            .setObjectBucket(configurationProperties.publicStoreBucketName())
//            .setObjectKey(command.objectKey())
//            .setOwnerId(command.accountProfileId());
//    var insertedStorageGrant = storageGrantsRepository.insert(storageGrant);
//
//    try {
//      s3Port.delete(command.objectKey(), configurationProperties.landingZoneBucketName());
//    } catch (Exception e) {
//      log.warn(
//          "Failed to delete object {} from bucket {}",
//          command.objectKey(),
//          configurationProperties.landingZoneBucketName());
//    }
//
//    return insertedStorageGrant;
//  }

  @Override
  public boolean validatePrivateKey(String key) {
    return key.startsWith(PRIVATE_KEY_PREFIX);
  }

  private String resolveObjectAccess(String objectKey, Boolean isPublic) {
    if (isPublic == Boolean.TRUE) {
      return PUBLIC_KEY_PREFIX + objectKey;
    } else {
      return PRIVATE_KEY_PREFIX + objectKey;
    }
  }
}
