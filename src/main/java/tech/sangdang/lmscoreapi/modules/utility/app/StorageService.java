package tech.sangdang.lmscoreapi.modules.utility.app;

import org.jspecify.annotations.Nullable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.generated.model.UploadToStorageCommand;
import tech.sangdang.lmscoreapi.generated.model.UploadToStorageResponse;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPrivateCommand;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;
import tech.sangdang.lmscoreapi.modules.utility.dom.StorageGrants;

/**
 * Landing-zone upload and public-store confirm.
 *
 * <p>Public file sequence: {@link #getPresignedUploadUrl} → client PUT to {@code uploadUrl} →
 * domain write calls {@link #confirmPublicFileUpload} (or a domain service that does).
 */
public interface StorageService {

  /**
   * Issues landing-zone presigned URLs and an assigned object key.
   *
   * <ul>
   *   <li>Always targets the landing zone; does not write the permanent public store.
   *   <li>{@code isPublic: true} → {@code public/{uuid}}; otherwise {@code private/{uuid}}.
   *   <li>Next: client PUT file to {@code uploadUrl}, then submit the key on a domain write (e.g.
   *       profile {@code avatarKey}, classroom {@code bannerKey}) which must call {@link
   *       #confirmPublicFileUpload}.
   * </ul>
   *
   * @param uploadToStorageCommand optional; null or omitted {@code isPublic} means private
   * @return upload URL, download URL, and object key
   */
  UploadToStorageResponse getPresignedUploadUrl(
      @Nullable UploadToStorageCommand uploadToStorageCommand);

  /**
   * Moves a public object from the landing zone into the permanent public store.
   *
   * <ul>
   *   <li>Call after the client has PUT the file to the landing zone and a domain write is
   *       committing that {@code objectKey}.
   *   <li>Idempotent when the object is already in the public store.
   *   <li>Best-effort deletes the landing-zone copy after copy; delete failure is logged only.
   *   <li>Must not run inside a DB transaction ({@code NEVER}).
   * </ul>
   *
   * @param command public landing-zone object key ({@code public/…})
   */
  @Transactional(propagation = Propagation.NEVER)
  void confirmPublicFileUpload(ConfirmUploadPublicCommand command);

  /**
   * Returns whether {@code key} is a public storage key ({@code public/} prefix).
   *
   * @param key object key to check
   * @return true if the key may be confirmed as public
   */
  boolean validatePublicKey(String key);

  /**
   * Moves a private object from the landing zone and records a storage grant.
   *
   * <ul>
   *   <li>Not implemented yet; default throws {@link UnsupportedOperationException}.
   *   <li>Intended next step after a private {@link #getPresignedUploadUrl} upload when private
   *       permanent transfer is wired.
   * </ul>
   *
   * @param command private landing-zone key and owner account profile id
   * @return created storage grant
   */
  default StorageGrants confirmPrivateFileUpload(ConfirmUploadPrivateCommand command) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Returns whether {@code key} is a private storage key ({@code private/} prefix).
   *
   * @param key object key to check
   * @return true if the key may be confirmed as private
   */
  boolean validatePrivateKey(String key);
}
