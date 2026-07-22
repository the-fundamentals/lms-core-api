package tech.sangdang.lmscoreapi.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.cognito")
public record CognitoProperties(
    String region,
    String userPoolId,
    String appClientId,
    String clientFullDomain,
    String baseDomain) {

  private static final String AUTHORIZATION_URL_PLACEHOLDER = "__COGNITO_DOMAIN__/oauth2/authorize";
  private static final String TOKEN_URL_PLACEHOLDER = "__COGNITO_DOMAIN__/oauth2/token";

  /**
   * Substitutes OAuth URL placeholders in the curated OpenAPI YAML so Swagger UI Authorize
   * redirects to this environment's Cognito Hosted UI.
   */
  public String applyOpenApiPlaceholders(String openApiYaml) {
    return openApiYaml
        .replace(AUTHORIZATION_URL_PLACEHOLDER, resolvedAuthorizationUrl())
        .replace(TOKEN_URL_PLACEHOLDER, resolvedTokenUrl());
  }

  private String resolvedAuthorizationUrl() {
    return hostedUiBaseUrl() + "/oauth2/authorize";
  }

  private String resolvedTokenUrl() {
    return hostedUiBaseUrl() + "/oauth2/token";
  }

  private String hostedUiBaseUrl() {
    if (!StringUtils.hasText(clientFullDomain)) {
      return "https://__COGNITO_DOMAIN__"; // PLACEHOLDER
    }
    String trimmed = clientFullDomain.trim().replaceAll("/$", "");
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
      return trimmed;
    }
    return "https://" + trimmed;
  }
}
