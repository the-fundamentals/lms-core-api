package tech.sangdang.lmscoreapi.modules.account.support;

import java.time.LocalDateTime;
import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;

public final class AccountProfileFixtures {

  public static final UUID PROFILE_ID = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
  public static final String COGNITO_SUB = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
  public static final String EMAIL = "jane.doe@example.com";
  public static final String FIRST_NAME = "Jane";
  public static final String LAST_NAME = "Doe";
  public static final String AVATAR_KEY = "public/3e4ef8d1-9581-402f-9e27-fec209aeb2d8";
  public static final String NEW_AVATAR_KEY = "public/540ba3e4-8026-45e9-9834-54aa0eb1a17a";
  public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 7, 14, 0, 0, 0);
  public static final LocalDateTime MODIFIED_AT = LocalDateTime.of(2026, 7, 14, 0, 0, 0);

  private AccountProfileFixtures() {}

  public static AccountProfile accountProfile() {
    return new AccountProfile()
        .setId(PROFILE_ID)
        .setCognitoSub(COGNITO_SUB)
        .setEmail(EMAIL)
        .setFirstName(FIRST_NAME)
        .setLastName(LAST_NAME)
        .setAvatarKey(AVATAR_KEY)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }
}
