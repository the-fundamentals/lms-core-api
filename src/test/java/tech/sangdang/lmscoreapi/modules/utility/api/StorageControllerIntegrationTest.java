package tech.sangdang.lmscoreapi.modules.utility.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.COGNITO_SUB;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.DOWNLOAD_URL;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.LANDING_ZONE_BUCKET;
import static tech.sangdang.lmscoreapi.modules.utility.support.StorageFixtures.UPLOAD_URL;

import java.time.Duration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.UploadToStorageCommand;
import tech.sangdang.lmscoreapi.modules.utility.app.impl.StorageServiceImpl;
import tech.sangdang.lmscoreapi.modules.utility.dom.ports.S3Port;
import tech.sangdang.lmscoreapi.modules.utility.dom.repository.StorageGrantsRepository;
import tech.sangdang.lmscoreapi.modules.utility.infra.StorageConfigurationProperties;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = StorageController.class)
@Import({
  GlobalExceptionHandler.class,
  StorageServiceImpl.class,
  SecurityConfig.class,
})
@EnableConfigurationProperties(StorageConfigurationProperties.class)
@TestPropertySource(
    properties = {
      "app.utility.storage.landing-zone-bucket-name=lms-local-landing-zone",
      "app.utility.storage.public-store-bucket-name=lms-local-public",
      "app.utility.storage.endpoint-override=http://localhost.localstack.cloud:4566",
      "app.utility.storage.region=ap-southeast-1",
      "app.utility.storage.use-dummy-credentials=true",
      "app.utility.storage.presigned-expiration-minutes=5",
    })
@DisplayName("Storage")
class StorageControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private S3Port s3Port;
  @MockitoBean private StorageGrantsRepository storageGrantsRepository;

  @BeforeEach
  void stubPresignedUrls() {
    when(s3Port.getUploadPresignedUrl(anyString(), anyString(), any(Duration.class)))
        .thenReturn(UPLOAD_URL);
    when(s3Port.getDownloadPresignedUrl(anyString(), anyString(), any(Duration.class)))
        .thenReturn(DOWNLOAD_URL);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "returns a public object key when isPublic is true, true, public/",
    "returns a private object key when isPublic is false, false, private/",
    "returns a private object key when the request body is omitted, , private/"
  })
  void uploadToStorage_returns200WithExpectedKeyPrefix(
      String displayName, Boolean isPublic, String keyPrefix) throws Exception {
    MockHttpServletRequestBuilder request =
        post("/private/storage").with(jwt().jwt(j -> j.subject(COGNITO_SUB)));
    if (isPublic != null) {
      UploadToStorageCommand command = UploadToStorageCommand.builder().isPublic(isPublic).build();
      request
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(command));
    }

    mockMvc
        .perform(request)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.objectKey", Matchers.startsWith(keyPrefix)))
        .andExpect(jsonPath("$.uploadUrl").value(UPLOAD_URL))
        .andExpect(jsonPath("$.downloadUrl").value(DOWNLOAD_URL));

    verify(s3Port)
        .getUploadPresignedUrl(
            eq(LANDING_ZONE_BUCKET),
            argThat(key -> key.startsWith(keyPrefix)),
            eq(Duration.ofMinutes(5)));
    verify(s3Port)
        .getDownloadPresignedUrl(
            eq(LANDING_ZONE_BUCKET),
            argThat(key -> key.startsWith(keyPrefix)),
            eq(Duration.ofMinutes(15)));
  }
}
