package tech.sangdang.lmscoreapi.modules;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.config.properties.CognitoProperties;

@RestController
@RequiredArgsConstructor
public class OpenApiSpecController {
  private static final String OPENAPI_CLASSPATH = "openapi.yml";
  private final CognitoProperties cognitoProperties;

  /**
   * Serves the OpenAPI contract so Swagger UI matches codegen. Cognito OAuth authorize/token URLs
   * are injected from {@link CognitoProperties} (env / application.yml).
   */
  @GetMapping(path = "/openapi.yml", produces = "application/yaml")
  public ResponseEntity<String> openApiYaml() throws IOException {
    String yaml =
        new ClassPathResource(OPENAPI_CLASSPATH).getContentAsString(StandardCharsets.UTF_8);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_YAML)
        .body(cognitoProperties.applyOpenApiPlaceholders(yaml));
  }
}
