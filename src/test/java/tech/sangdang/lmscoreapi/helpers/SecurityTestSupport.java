package tech.sangdang.lmscoreapi.helpers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class SecurityTestSupport {

  private SecurityTestSupport() {}

  /** Mock JWT with {@code ROLE_ADMIN} for `/admin/**` MockMvc calls. */
  public static RequestPostProcessor adminJwt() {
    return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }
}
