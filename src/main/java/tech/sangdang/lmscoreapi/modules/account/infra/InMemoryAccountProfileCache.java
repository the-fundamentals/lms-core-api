package tech.sangdang.lmscoreapi.modules.account.infra;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * In-memory stand-in for an account-profile cache backed by an authentication service. Seeded with
 * sample profiles for local development.
 */
@Component
public class InMemoryAccountProfileCache implements AccountProfileCache {

  private final Map<String, AccountProfile> profilesByAccountId = new ConcurrentHashMap<>();

  public InMemoryAccountProfileCache() {
    put(new AccountProfile("acc_12345", "alex@example.com", "Alex Nguyen"));
    put(new AccountProfile("acc_67890", "sam@example.com", "Sam Chen"));
  }

  @Override
  public Optional<AccountProfile> findByAccountId(@NonNull String accountId) {
    return Optional.ofNullable(profilesByAccountId.get(accountId));
  }

  public void put(@NonNull AccountProfile profile) {
    profilesByAccountId.put(profile.accountId(), profile);
  }
}
