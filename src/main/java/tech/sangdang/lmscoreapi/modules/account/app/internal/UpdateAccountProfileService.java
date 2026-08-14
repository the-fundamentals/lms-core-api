package tech.sangdang.lmscoreapi.modules.account.app.internal;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.InternalService;
import tech.sangdang.lmscoreapi.generated.model.UpdateAccountProfileCommand;
import tech.sangdang.lmscoreapi.modules.account.app.dto.TokenClaims;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;

import java.util.Objects;

@RequiredArgsConstructor
@InternalService
public class UpdateAccountProfileService {
  private final AccountProfileRepository accountProfileRepository;

  @Transactional
  public AccountProfile updateAccountProfile(
          @NonNull UpdateAccountProfileCommand command, @NonNull String cognitoSub, @NonNull TokenClaims tokenClaims) {
    AccountProfile profile =
        accountProfileRepository.findByCognitoSub(cognitoSub).orElse(new AccountProfile());

    profile.setFirstName(command.getFirstName());
    profile.setLastName(command.getLastName());
    profile.setCognitoSub(cognitoSub);
    profile.setEmail(tokenClaims.email());
    profile.setAvatarKey(command.getAvatarKey());

    if (Objects.nonNull(profile.getId())) {
      return accountProfileRepository.update(profile);
    }
    return accountProfileRepository.insert(profile);
  }
}
