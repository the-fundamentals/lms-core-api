package tech.sangdang.lmscoreapi.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class CurrentUser {

  private CurrentUser() {}

  /** Cognito user id from the JWT {@code sub} claim. */
  public static String requireCognitoSub() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      String subject = jwtAuth.getToken().getSubject();
      if (subject != null && !subject.isBlank()) {
        return subject;
      }
    }
    throw new IllegalStateException("Authenticated JWT with a subject claim is required");
  }
}
