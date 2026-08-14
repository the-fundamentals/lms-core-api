package tech.sangdang.lmscoreapi.modules.account.app.impl;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.InvalidIdTokenException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.common.security.CurrentUser;
import tech.sangdang.lmscoreapi.generated.model.AccountProfileResponse;
import tech.sangdang.lmscoreapi.generated.model.UpdateAccountProfileCommand;
import tech.sangdang.lmscoreapi.modules.account.app.AccountProfileService;
import tech.sangdang.lmscoreapi.modules.account.app.dto.TokenClaims;
import tech.sangdang.lmscoreapi.modules.account.app.internal.UpdateAccountProfileService;
import tech.sangdang.lmscoreapi.modules.account.app.mappers.AccountProfileMapper;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.dom.ports.TokenUtilityPort;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;
import tech.sangdang.lmscoreapi.modules.utility.app.StorageService;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;

@Service
@RequiredArgsConstructor
public class AccountProfileServiceImpl implements AccountProfileService {

  private final AccountProfileRepository accountProfileRepository;
  private final AccountProfileMapper accountProfileMapper;
  private final TokenUtilityPort tokenUtilityPort;
  private final StorageService storageService;

  private final UpdateAccountProfileService updateAccountProfileService;

  @Override
  @Transactional(readOnly = true)
  public AccountProfileResponse getMyAccountProfile() {
    String cognitoSub = CurrentUser.requireCognitoSub();
    return accountProfileRepository
        .findByCognitoSub(cognitoSub)
        .map(accountProfileMapper::toResponse)
        .orElseThrow(() -> ObjectNotFoundException.of(AccountProfile.class, cognitoSub));
  }

  @Override
  public AccountProfileResponse updateMyAccountProfile(
      UpdateAccountProfileCommand command, String idToken) {

    // Fetch & Validate the current ID Token details
    String cognitoSub = CurrentUser.requireCognitoSub();
    TokenClaims tokenClaims = tokenUtilityPort.validateAndDecodeIdToken(idToken);
    if (!Objects.equals(tokenClaims.sub(), cognitoSub)) {
      throw InvalidIdTokenException.of("ID token sub does not match the authenticated user");
    }

    // Update the database
    AccountProfile accountProfile =
        updateAccountProfileService.updateAccountProfile(command, cognitoSub, tokenClaims);

    // Move S3 file to Public Bucket
    storageService.confirmPublicFileUpload(new ConfirmUploadPublicCommand(command.getAvatarKey()));

    return accountProfileMapper.toResponse(accountProfile);
  }
}
