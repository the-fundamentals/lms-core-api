package tech.sangdang.lmscoreapi.modules.utility.app;

import org.jspecify.annotations.Nullable;
import tech.sangdang.lmscoreapi.generated.model.UploadToStorageCommand;
import tech.sangdang.lmscoreapi.generated.model.UploadToStorageResponse;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPrivateCommand;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;
import tech.sangdang.lmscoreapi.modules.utility.dom.StorageGrants;

public interface StorageService {
  UploadToStorageResponse getPresignedUploadUrl(
      @Nullable UploadToStorageCommand uploadToStorageCommand);

  void confirmPublicFileUpload(ConfirmUploadPublicCommand command);

  boolean validatePublicKey(String key);

  default StorageGrants confirmPrivateFileUpload(ConfirmUploadPrivateCommand command) {
    throw new UnsupportedOperationException("Not supported yet.");
  };

  boolean validatePrivateKey(String key);
}
