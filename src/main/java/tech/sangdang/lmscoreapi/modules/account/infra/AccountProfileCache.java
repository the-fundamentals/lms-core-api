package tech.sangdang.lmscoreapi.modules.account.infra;

import java.util.Optional;
import org.jspecify.annotations.NonNull;

/** Cache of account profile data (normally populated from an authentication / identity service). */
public interface AccountProfileCache {

  Optional<AccountProfile> findByAccountId(@NonNull String accountId);
}
