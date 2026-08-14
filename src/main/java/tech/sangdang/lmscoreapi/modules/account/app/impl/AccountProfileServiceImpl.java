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
import tech.sangdang.lmscoreapi.modules.account.app.mappers.AccountProfileMapper;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.dom.ports.TokenUtilityPort;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;
import tech.sangdang.lmscoreapi.modules.utility.app.StorageService;

@Service
@RequiredArgsConstructor
public class AccountProfileServiceImpl implements AccountProfileService {

  private final AccountProfileRepository accountProfileRepository;
  private final AccountProfileMapper accountProfileMapper;
  private final TokenUtilityPort tokenUtilityPort;
  private final StorageService storageService;

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
  @Transactional
  public AccountProfileResponse updateMyAccountProfile(
      UpdateAccountProfileCommand command, String idToken) {
    String cognitoSub = CurrentUser.requireCognitoSub();
    TokenClaims tokenClaims = tokenUtilityPort.validateAndDecodeIdToken(idToken);
    if (!Objects.equals(tokenClaims.sub(), cognitoSub)) {
      throw InvalidIdTokenException.of("ID token sub does not match the authenticated user");
    }

    AccountProfile profile =
        accountProfileRepository.findByCognitoSub(cognitoSub).orElse(new AccountProfile());

    profile.setFirstName(command.getFirstName());
    profile.setLastName(command.getLastName());
    profile.setCognitoSub(cognitoSub);
    profile.setEmail(tokenClaims.email());
    profile.setAvatarKey(command.getAvatarKey());

    if (Objects.nonNull(profile.getId())) {
      return accountProfileMapper.toResponse(accountProfileRepository.update(profile));
    }
    return accountProfileMapper.toResponse(accountProfileRepository.insert(profile));
  }
}
