package tech.sangdang.lmscoreapi.modules.account.api;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.AccountsApi;
import tech.sangdang.lmscoreapi.generated.model.AccountFilter;
import tech.sangdang.lmscoreapi.modules.account.app.AccountService;

@RestController
@RequiredArgsConstructor
public class AccountController implements AccountsApi {

  private final AccountService accountService;

  @Override
  public ResponseEntity<?> getAllAccounts(@NonNull AccountFilter accountFilter) {
    return ResponseEntity.ok(accountService.queryAccounts(accountFilter));
  }
}
