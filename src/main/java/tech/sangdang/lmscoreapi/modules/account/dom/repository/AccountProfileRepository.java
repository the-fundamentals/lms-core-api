package tech.sangdang.lmscoreapi.modules.account.dom.repository;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;

public interface AccountProfileRepository
    extends BaseCommandRepository<AccountProfile, UUID>, BaseQueryRepository<AccountProfile, UUID> {

  @Query(
      """
      SELECT * FROM account_profile
      WHERE cognito_sub = :cognitoSub
      """)
  Optional<AccountProfile> findByCognitoSub(@NonNull @Param("cognitoSub") String cognitoSub);
}
