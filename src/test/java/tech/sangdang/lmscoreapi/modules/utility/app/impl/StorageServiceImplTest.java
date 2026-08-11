package tech.sangdang.lmscoreapi.modules.utility.app.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.BAD_PRIVATE_OBJECT_KEY;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.BAD_PUBLIC_OBJECT_KEY;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.LANDING_ZONE_BUCKET;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.OWNER_ID;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.PRIVATE_OBJECT_KEY;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.PUBLIC_OBJECT_KEY;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.PUBLIC_STORE_BUCKET;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.storageGrant;

import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPrivateCommand;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;
import tech.sangdang.lmscoreapi.modules.utility.dom.StorageGrants;
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
            5,
            null);
    storageService = new StorageServiceImpl(s3Port, properties, storageGrantsRepository);
  }

  @Test
  @DisplayName("confirms a public file upload")
  void confirmPublicFileUpload_valid_copiesAndDeletes() {
    when(s3Port.exists(PUBLIC_OBJECT_KEY, LANDING_ZONE_BUCKET)).thenReturn(true);

    storageService.confirmPublicFileUpload(new ConfirmUploadPublicCommand(PUBLIC_OBJECT_KEY));

    verify(s3Port)
        .copy(PUBLIC_OBJECT_KEY, LANDING_ZONE_BUCKET, PUBLIC_OBJECT_KEY, PUBLIC_STORE_BUCKET);
    verify(s3Port).delete(PUBLIC_OBJECT_KEY, LANDING_ZONE_BUCKET);
    verify(storageGrantsRepository, never()).insert(any());
  }

  @Test
  @DisplayName("confirms a private file upload and records a grant")
  void confirmPrivateFileUpload_valid_copiesDeletesAndInsertsGrant() {
    when(s3Port.exists(PRIVATE_OBJECT_KEY, LANDING_ZONE_BUCKET)).thenReturn(true);
    when(storageGrantsRepository.insert(any(StorageGrants.class)))
        .thenAnswer(
            invocation -> {
              StorageGrants incoming = invocation.getArgument(0);
              return storageGrant(incoming.getObjectKey());
            });

    StorageGrants result =
        storageService.confirmPrivateFileUpload(
            new ConfirmUploadPrivateCommand(PRIVATE_OBJECT_KEY, OWNER_ID));

    assertThat(result.getObjectKey()).isEqualTo(PRIVATE_OBJECT_KEY);
    assertThat(result.getOwnerId()).isEqualTo(OWNER_ID);
    assertThat(result.getObjectBucket()).isEqualTo(PUBLIC_STORE_BUCKET);

    verify(s3Port)
        .copy(PRIVATE_OBJECT_KEY, LANDING_ZONE_BUCKET, PRIVATE_OBJECT_KEY, PUBLIC_STORE_BUCKET);
    verify(s3Port).delete(PRIVATE_OBJECT_KEY, LANDING_ZONE_BUCKET);

    ArgumentCaptor<StorageGrants> captor = ArgumentCaptor.forClass(StorageGrants.class);
    verify(storageGrantsRepository).insert(captor.capture());
    assertThat(captor.getValue().getOwnerId()).isEqualTo(OWNER_ID);
    assertThat(captor.getValue().getObjectKey()).isEqualTo(PRIVATE_OBJECT_KEY);
    assertThat(captor.getValue().getObjectBucket()).isEqualTo(PUBLIC_STORE_BUCKET);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("badKeyCases")
  void confirmFileUpload_badKey_rejectsWithoutTouchingStorage(
      String displayName, Consumer<StorageServiceImpl> action, String expectedMessage) {
    assertThatThrownBy(() -> action.accept(storageService))
        .isInstanceOfSatisfying(
            GenericBadRequestException.class,
            ex -> {
              assertThat(ex.getCode()).isEqualTo("BAD_KEY");
              assertThat(ex.getMessage()).isEqualTo(expectedMessage);
            });

    verify(s3Port, never()).exists(any(), any());
    verify(s3Port, never()).copy(any(), any(), any(), any());
    verify(storageGrantsRepository, never()).insert(any());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("missingObjectCases")
  void confirmFileUpload_missingObject_throwsNotFound(
      String displayName, Consumer<StorageServiceImpl> action, String objectKey) {
    when(s3Port.exists(objectKey, LANDING_ZONE_BUCKET)).thenReturn(false);

    assertThatThrownBy(() -> action.accept(storageService))
        .isInstanceOfSatisfying(
            ObjectNotFoundException.class,
            ex -> {
              assertThat(ex.getCode()).isEqualTo("STORAGE_OBJECT_NOT_FOUND");
              assertThat(ex.getMessage()).isEqualTo("StorageObject not found: " + objectKey);
            });

    verify(s3Port, never()).copy(any(), any(), any(), any());
    verify(storageGrantsRepository, never()).insert(any());
  }

  private static Stream<Arguments> badKeyCases() {
    return Stream.of(
        Arguments.of(
            "rejects a public confirm with a non-public key",
            (Consumer<StorageServiceImpl>)
                service ->
                    service.confirmPublicFileUpload(
                        new ConfirmUploadPublicCommand(BAD_PUBLIC_OBJECT_KEY)),
            "Invalid public key"),
        Arguments.of(
            "rejects a private confirm with a non-private key",
            (Consumer<StorageServiceImpl>)
                service ->
                    service.confirmPrivateFileUpload(
                        new ConfirmUploadPrivateCommand(BAD_PRIVATE_OBJECT_KEY, OWNER_ID)),
            "Invalid private key"));
  }

  private static Stream<Arguments> missingObjectCases() {
    return Stream.of(
        Arguments.of(
            "fails to confirm a public upload that does not exist",
            (Consumer<StorageServiceImpl>)
                service ->
                    service.confirmPublicFileUpload(
                        new ConfirmUploadPublicCommand(PUBLIC_OBJECT_KEY)),
            PUBLIC_OBJECT_KEY),
        Arguments.of(
            "fails to confirm a private upload that does not exist",
            (Consumer<StorageServiceImpl>)
                service ->
                    service.confirmPrivateFileUpload(
                        new ConfirmUploadPrivateCommand(PRIVATE_OBJECT_KEY, OWNER_ID)),
            PRIVATE_OBJECT_KEY));
  }
}
