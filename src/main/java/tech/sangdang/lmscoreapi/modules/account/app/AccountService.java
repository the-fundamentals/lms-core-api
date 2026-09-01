package tech.sangdang.lmscoreapi.modules.account.app;

import java.util.List;
import tech.sangdang.lmscoreapi.generated.model.AccountFilter;
import tech.sangdang.lmscoreapi.generated.model.AccountProfileResponse;

public interface AccountService {

  List<AccountProfileResponse> queryAccounts(AccountFilter filter);
}
