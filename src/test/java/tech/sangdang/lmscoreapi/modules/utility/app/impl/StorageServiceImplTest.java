package tech.sangdang.lmscoreapi.modules.utility.app.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.BAD_PUBLIC_OBJECT_KEY;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.LANDING_ZONE_BUCKET;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.PUBLIC_OBJECT_KEY;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.PUBLIC_STORE_BUCKET;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;
import tech.sangdang.lmscoreapi.modules.utility.dom.ports.S3Port;
import tech.sangdang.lmscoreapi.modules.utility.dom.repository.StorageGrantsRepository;
import tech.sangdang.lmscoreapi.modules.utility.infra.StorageConfigurationProperties;

@ExtendWith(MockitoExtension.class)
@DisplayName("Storage service")
class StorageServiceImplTest {

  @Mock private S3Port s3Port;
  @Mock private StorageGrantsRepository storageGrantsRepository;

  private StorageServiceImpl storageService;

  @BeforeEach
  void setUp() {
    StorageConfigurationProperties properties =
        new StorageConfigurationProperties(
            LANDING_ZONE_BUCKET,
            PUBLIC_STORE_BUCKET,
            "http://localhost.localstack.cloud:4566",
            "ap-southeast-1",
            true,
            null);
    storageService = new StorageServiceImpl(s3Port, properties, storageGrantsRepository);
  }

  @Test
  @DisplayName("confirms a public file upload")
  void confirmPublicFileUpload_valid_copiesAndDeletes() {
    when(s3Port.exists(PUBLIC_OBJECT_KEY, PUBLIC_STORE_BUCKET)).thenReturn(false);
    when(s3Port.exists(PUBLIC_OBJECT_KEY, LANDING_ZONE_BUCKET)).thenReturn(true);

    storageService.confirmPublicFileUpload(new ConfirmUploadPublicCommand(PUBLIC_OBJECT_KEY));

    verify(s3Port)
        .copy(PUBLIC_OBJECT_KEY, LANDING_ZONE_BUCKET, PUBLIC_OBJECT_KEY, PUBLIC_STORE_BUCKET);
    verify(s3Port).delete(PUBLIC_OBJECT_KEY, LANDING_ZONE_BUCKET);
    verify(storageGrantsRepository, never()).insert(any());
  }

  @Test
  @DisplayName("skips copy when the public object already exists")
  void confirmPublicFileUpload_alreadyInPublic_isIdempotent() {
    when(s3Port.exists(PUBLIC_OBJECT_KEY, PUBLIC_STORE_BUCKET)).thenReturn(true);

    storageService.confirmPublicFileUpload(new ConfirmUploadPublicCommand(PUBLIC_OBJECT_KEY));

    verify(s3Port, never()).exists(PUBLIC_OBJECT_KEY, LANDING_ZONE_BUCKET);
    verify(s3Port, never()).copy(any(), any(), any(), any());
    verify(s3Port, never()).delete(any(), any());
  }

  @Test
  @DisplayName("rejects a public confirm with a non-public key")
  void confirmPublicFileUpload_badKey_rejectsWithoutTouchingStorage() {
    assertThatThrownBy(
            () ->
                storageService.confirmPublicFileUpload(
                    new ConfirmUploadPublicCommand(BAD_PUBLIC_OBJECT_KEY)))
        .isInstanceOfSatisfying(
            GenericBadRequestException.class,
            ex -> {
              assertThat(ex.getCode()).isEqualTo("BAD_KEY");
              assertThat(ex.getMessage()).isEqualTo("Invalid public key");
            });

    verify(s3Port, never()).exists(any(), any());
    verify(s3Port, never()).copy(any(), any(), any(), any());
    verify(storageGrantsRepository, never()).insert(any());
  }

  @Test
  @DisplayName("fails to confirm a public upload that does not exist")
  void confirmPublicFileUpload_missingObject_throwsNotFound() {
    when(s3Port.exists(PUBLIC_OBJECT_KEY, PUBLIC_STORE_BUCKET)).thenReturn(false);
    when(s3Port.exists(PUBLIC_OBJECT_KEY, LANDING_ZONE_BUCKET)).thenReturn(false);

    assertThatThrownBy(
            () ->
                storageService.confirmPublicFileUpload(
                    new ConfirmUploadPublicCommand(PUBLIC_OBJECT_KEY)))
        .isInstanceOfSatisfying(
            ObjectNotFoundException.class,
            ex -> {
              assertThat(ex.getCode()).isEqualTo("STORAGE_OBJECT_NOT_FOUND");
              assertThat(ex.getMessage())
                  .isEqualTo("StorageObject not found: " + PUBLIC_OBJECT_KEY);
            });

    verify(s3Port, never()).copy(any(), any(), any(), any());
    verify(storageGrantsRepository, never()).insert(any());
  }
}
