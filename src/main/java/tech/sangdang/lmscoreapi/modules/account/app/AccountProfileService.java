package tech.sangdang.lmscoreapi.modules.account.app;

import tech.sangdang.lmscoreapi.generated.model.AccountProfileResponse;
import tech.sangdang.lmscoreapi.generated.model.UpdateAccountProfileCommand;

public interface AccountProfileService {

  /**
   * Returns the account profile for the authenticated Cognito user.
   *
   * @return current user's profile
   */
  AccountProfileResponse getMyAccountProfile();

  /**
   * Upserts the authenticated user's profile and confirms {@code avatarKey} in public storage.
   *
   * <ul>
   *   <li>Prerequisite: client obtained {@code avatarKey} via {@code
   *       StorageService#getPresignedUploadUrl} ({@code isPublic: true}) and PUT the file to the
   *       landing zone.
   *   <li>Then calls {@code StorageService#confirmPublicFileUpload} (landing zone → permanent
   *       public store; idempotent if already there).
   *   <li>Email comes from {@code idToken}; access-token {@code sub} must match.
   * </ul>
   *
   * @param command names and public avatar object key
   * @param idToken Cognito ID token ({@code X-ID-Token})
   * @return created or updated profile
   */
  AccountProfileResponse updateMyAccountProfile(
      UpdateAccountProfileCommand command, String idToken);
}
