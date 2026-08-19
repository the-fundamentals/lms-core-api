package tech.sangdang.lmscoreapi.modules.account.infra;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import tech.sangdang.lmscoreapi.common.GeneralConfigProperties;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;

@Profile("local")
@Slf4j
@RequiredArgsConstructor
@Component
public class LoadDatabaseWithDummyAccounts {
  private final AccountProfileRepository accountProfileRepository;
  private final GeneralConfigProperties generalConfigProperties;

  @Bean
  public CommandLineRunner run() {
    return args -> {
      // skip if disabled
      if (Objects.isNull(generalConfigProperties.loadDummyAccountsIntoDatabase())
          || !generalConfigProperties.loadDummyAccountsIntoDatabase()) {
        return;
      }

      log.info("Loading dummy accounts into the database");

      List<AccountProfile> accounts =
          List.of(
              new AccountProfile()
                  .setId(UUID.fromString("7d80e316-9a59-459e-9710-a84416ba9adc"))
                  .setFirstName("John")
                  .setLastName("Dummy")
                  .setEmail("john.dummy@email.com")
                  .setCognitoSub("1528791e-a96c-4dcf-be1d-f24565e0819c")
                  .setAvatarKey(null),
              new AccountProfile()
                  .setId(UUID.fromString("2a9a9f4d-71ed-4139-8f4e-6bcfde5ead41"))
                  .setFirstName("Alice")
                  .setLastName("Dummy")
                  .setEmail("alice.dummy@email.com")
                  .setCognitoSub("017f595e-b9ea-4abb-83be-6270c83b99ae")
                  .setAvatarKey(null),
              new AccountProfile()
                  .setId(UUID.fromString("95ffc4f5-9591-4eb9-ac7f-3d01a3e5246c"))
                  .setFirstName("Bob")
                  .setLastName("Dummy")
                  .setEmail("bob.dummy@email.com")
                  .setCognitoSub("21273647-0f16-45e8-98ce-85b49d6ffd9a")
                  .setAvatarKey(null),
              new AccountProfile()
                  .setId(UUID.fromString("5802e28e-d6e9-4ed0-9d4f-6f68dfd426df"))
                  .setFirstName("Carol")
                  .setLastName("Dummy")
                  .setEmail("carol.dummy@email.com")
                  .setCognitoSub("e4facb37-58ac-41a4-8e42-faef3227a839")
                  .setAvatarKey(null),
              new AccountProfile()
                  .setId(UUID.fromString("3b69e88a-48ff-48ac-aa87-ff6d272c8030"))
                  .setFirstName("Dave")
                  .setLastName("Dummy")
                  .setEmail("dave.dummy@email.com")
                  .setCognitoSub("d3000641-334b-45f0-a430-ca612524141f")
                  .setAvatarKey(null),
              new AccountProfile()
                  .setId(UUID.fromString("607b6b68-bad0-4809-a144-f0255e64f7c7"))
                  .setFirstName("Eve")
                  .setLastName("Dummy")
                  .setEmail("eve.dummy@email.com")
                  .setCognitoSub("900ee492-6e95-41e0-97ca-9c930789ccf2")
                  .setAvatarKey(null));

      try {
        List<AccountProfile> toInsert =
            accounts.stream()
                .filter(a -> accountProfileRepository.findByCognitoSub(a.getCognitoSub()).isEmpty())
                .toList();

        accountProfileRepository.insertAll(toInsert);
        log.info(
            "Inserted {} dummy account profiles, skipped {}",
            toInsert.size(),
            accounts.size() - toInsert.size());
      } catch (DataAccessException e) {
        log.error("Failed to insert dummy accounts", e);
      }
    };
  }
}
