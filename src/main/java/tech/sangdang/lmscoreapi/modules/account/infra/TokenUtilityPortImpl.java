package tech.sangdang.lmscoreapi.modules.account.infra;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tech.sangdang.lmscoreapi.common.exception.InvalidIdTokenException;
import tech.sangdang.lmscoreapi.modules.account.app.dto.TokenClaims;
import tech.sangdang.lmscoreapi.modules.account.dom.ports.TokenUtilityPort;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenUtilityPortImpl implements TokenUtilityPort {
  private final JwtDecoder jwtDecoder;

  private static final String COGNITO_ID_TOKEN_EMAIL_KEY = "email";
  private static final String COGNITO_ID_TOKEN_GROUPS_KEY = "cognito:groups";
  private static final String COGNITO_ID_TOKEN_AUTH_TIME_KEY = "auth_time";
  private static final String COGNITO_ID_TOKEN_TOKEN_USE_KEY = "token_use";

  @Override
  public TokenClaims validateAndDecodeIdToken(String idToken) {
    final Jwt jwt;
    try {
      jwt = jwtDecoder.decode(idToken);
    } catch (JwtException e) {
      throw InvalidIdTokenException.of("ID token could not be validated", e);
    }

    if (!Objects.equals(jwt.getClaimAsString(COGNITO_ID_TOKEN_TOKEN_USE_KEY), "id")) {
      throw InvalidIdTokenException.of("token_use must be id");
    }

    TokenClaims tokenClaims =
        new TokenClaims(
            jwt.getSubject(),
            jwt.getClaimAsString(COGNITO_ID_TOKEN_EMAIL_KEY),
            jwt.getClaimAsStringList(COGNITO_ID_TOKEN_GROUPS_KEY),
            jwt.getClaimAsInstant(COGNITO_ID_TOKEN_AUTH_TIME_KEY));

    validateTokenClaims(tokenClaims);

    return tokenClaims;
  }

  private void validateTokenClaims(TokenClaims claims) {
    if (!StringUtils.hasText(claims.sub())) {
      log.error("ID Token is missing the sub field");
      throw InvalidIdTokenException.of("ID token is missing the sub claim");
    }

    if (!StringUtils.hasText(claims.email())) {
      log.error("ID Token is missing the Email field");
      throw InvalidIdTokenException.of("ID token is missing the email claim");
    }

    if (claims.roles() == null) {
      log.error("ID Token is missing the Roles field");
      throw InvalidIdTokenException.of("ID token is missing the cognito:groups claim");
    }

    if (claims.authTime() == null) {
      log.error("ID Token is missing the Auth Time field");
      throw InvalidIdTokenException.of("ID token is missing the auth_time claim");
    }
  }
}
