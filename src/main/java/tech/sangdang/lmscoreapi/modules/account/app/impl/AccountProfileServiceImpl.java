package tech.sangdang.lmscoreapi.modules.account.app.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.common.security.CurrentUser;
import tech.sangdang.lmscoreapi.generated.model.AccountProfileResponse;
import tech.sangdang.lmscoreapi.modules.account.app.AccountProfileService;
import tech.sangdang.lmscoreapi.modules.account.app.mappers.AccountProfileMapper;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;

@Service
@RequiredArgsConstructor
public class AccountProfileServiceImpl implements AccountProfileService {

  private final AccountProfileRepository accountProfileRepository;
  private final AccountProfileMapper accountProfileMapper;

  @Override
  @Transactional(readOnly = true)
  public AccountProfileResponse getMyAccountProfile() {
    String cognitoSub = CurrentUser.requireCognitoSub();
    return accountProfileMapper.toResponse(
        accountProfileRepository
            .findByCognitoSub(cognitoSub)
            .orElseThrow(() -> ObjectNotFoundException.of(AccountProfile.class, cognitoSub)));
  }
}
