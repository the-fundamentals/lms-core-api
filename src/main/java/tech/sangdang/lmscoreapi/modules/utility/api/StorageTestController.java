package tech.sangdang.lmscoreapi.modules.utility.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import tech.sangdang.lmscoreapi.common.TestController;
import tech.sangdang.lmscoreapi.generated.api.TestApi;
import tech.sangdang.lmscoreapi.generated.model.ConfirmPrivateFileUploadRequest;
import tech.sangdang.lmscoreapi.generated.model.ConfirmPublicFileUploadRequest;
import tech.sangdang.lmscoreapi.modules.utility.app.StorageService;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPrivateCommand;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;

@TestController
@RequiredArgsConstructor
public class StorageTestController implements TestApi {

  private final StorageService storageService;

  @Override
  public ResponseEntity<?> confirmPrivateFileUpload(
      ConfirmPrivateFileUploadRequest confirmPrivateFileUploadRequest) {
    var command =
        new ConfirmUploadPrivateCommand(
            confirmPrivateFileUploadRequest.getObjectKey(),
            confirmPrivateFileUploadRequest.getAccountProfileId());
    return ResponseEntity.ok(storageService.confirmPrivateFileUpload(command));
  }

  @Override
  public ResponseEntity<?> confirmPublicFileUpload(
      ConfirmPublicFileUploadRequest confirmPublicFileUploadRequest) {
    var command = new ConfirmUploadPublicCommand(confirmPublicFileUploadRequest.getObjectKey());
    storageService.confirmPublicFileUpload(command);
    return ResponseEntity.noContent().build();
  }
}
