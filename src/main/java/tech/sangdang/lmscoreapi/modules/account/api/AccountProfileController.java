package tech.sangdang.lmscoreapi.modules.account.api;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.AccountProfileApi;
import tech.sangdang.lmscoreapi.generated.model.UpdateAccountProfileCommand;
import tech.sangdang.lmscoreapi.modules.account.app.AccountProfileService;

@RestController
@RequiredArgsConstructor
public class AccountProfileController implements AccountProfileApi {

  private final AccountProfileService accountProfileService;

  @Override
  @NullMarked
  public ResponseEntity<?> getMyAccountProfile() {
    return ResponseEntity.ok(accountProfileService.getMyAccountProfile());
  }

  @Override
  @NullMarked
  public ResponseEntity<?> updateMyAccountProfile(
      String xIDToken, UpdateAccountProfileCommand updateAccountProfileCommand) {
    return ResponseEntity.ok(
        accountProfileService.updateMyAccountProfile(updateAccountProfileCommand, xIDToken));
  }
}
