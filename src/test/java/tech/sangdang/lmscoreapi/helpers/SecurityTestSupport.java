package tech.sangdang.lmscoreapi.helpers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class SecurityTestSupport {

  private SecurityTestSupport() {}

  /**
   * Cognito {@code sub} used by {@link #adminJwt()} — match account profile fixtures when needed.
   */
  public static final String ADMIN_JWT_SUB = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

  /** Mock JWT with {@code ROLE_ADMIN} for `/admin/**` MockMvc calls. */
  public static RequestPostProcessor adminJwt() {
    return jwt()
        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
        .jwt(j -> j.subject(ADMIN_JWT_SUB));
  }
}
