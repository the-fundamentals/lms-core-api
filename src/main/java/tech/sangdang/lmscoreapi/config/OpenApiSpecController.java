package tech.sangdang.lmscoreapi.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenApiSpecController {

  private static final MediaType APPLICATION_YAML = MediaType.parseMediaType("application/yaml");

  /**
   * Serves the hand-written OpenAPI contract so Swagger UI matches codegen (including bearerAuth).
   */
  @GetMapping(path = "/openapi.yml", produces = "application/yaml")
  public ResponseEntity<Resource> openApiYaml() {
    return ResponseEntity.ok()
        .contentType(APPLICATION_YAML)
        .body(new ClassPathResource("openapi.yml"));
  }
}
