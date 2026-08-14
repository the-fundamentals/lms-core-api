package tech.sangdang.lmscoreapi.modules.utility.api;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.StorageApi;
import tech.sangdang.lmscoreapi.generated.model.UploadToStorageCommand;
import tech.sangdang.lmscoreapi.modules.utility.app.StorageService;

@RestController
@RequiredArgsConstructor
public class StorageController implements StorageApi {
  private final StorageService storageService;

  @Override
  public ResponseEntity<?> uploadToStorage(
      @Nullable UploadToStorageCommand uploadToStorageCommand) {
    return ResponseEntity.ok(storageService.getPresignedUploadUrl(uploadToStorageCommand));
  }
}
