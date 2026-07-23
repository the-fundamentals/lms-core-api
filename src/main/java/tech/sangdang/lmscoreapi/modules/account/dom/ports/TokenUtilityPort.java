package tech.sangdang.lmscoreapi.modules.account.dom.ports;

import tech.sangdang.lmscoreapi.modules.account.app.dto.TokenClaims;

public interface TokenUtilityPort {
  TokenClaims validateAndDecodeIdToken(String idToken);
}
