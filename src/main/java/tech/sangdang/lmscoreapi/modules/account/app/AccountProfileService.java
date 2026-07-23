package tech.sangdang.lmscoreapi.modules.account.app;

import tech.sangdang.lmscoreapi.generated.model.AccountProfileResponse;
import tech.sangdang.lmscoreapi.generated.model.UpdateAccountProfileCommand;

public interface AccountProfileService {

  AccountProfileResponse getMyAccountProfile();

  AccountProfileResponse updateMyAccountProfile(
      UpdateAccountProfileCommand command, String idToken);
}
