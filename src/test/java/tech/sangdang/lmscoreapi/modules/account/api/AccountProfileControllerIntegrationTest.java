package tech.sangdang.lmscoreapi.modules.account.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.COGNITO_SUB;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.EMAIL;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.FIRST_NAME;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.LAST_NAME;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.PROFILE_ID;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.accountProfile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.common.exception.InvalidIdTokenException;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.UpdateAccountProfileCommand;
import tech.sangdang.lmscoreapi.modules.account.app.dto.TokenClaims;
import tech.sangdang.lmscoreapi.modules.account.app.impl.AccountProfileServiceImpl;
import tech.sangdang.lmscoreapi.modules.account.app.mappers.AccountProfileMapperImpl;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.dom.ports.TokenUtilityPort;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = AccountProfileController.class)
@Import({
  GlobalExceptionHandler.class,
  AccountProfileServiceImpl.class,
  AccountProfileMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Account profile")
class AccountProfileControllerIntegrationTest {

  private static final String ID_TOKEN = "test-id-token";

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private AccountProfileRepository accountProfileRepository;
  @MockitoBean private TokenUtilityPort tokenUtilityPort;

  @Test
  @DisplayName("returns the profile for the authenticated Cognito user")
  void getMyAccountProfile_exists_returns200() throws Exception {
    when(accountProfileRepository.findByCognitoSub(COGNITO_SUB))
        .thenReturn(Optional.of(accountProfile()));

    mockMvc
        .perform(get("/private/profile").with(jwt().jwt(j -> j.subject(COGNITO_SUB))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(PROFILE_ID.toString()))
        .andExpect(jsonPath("$.cognitoSub").value(COGNITO_SUB))
        .andExpect(jsonPath("$.email").value(EMAIL))
        .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
        .andExpect(jsonPath("$.lastName").value(LAST_NAME))
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());
  }

  @Test
  @DisplayName("fails when no profile exists yet for the Cognito user")
  void getMyAccountProfile_missing_returns404() throws Exception {
    when(accountProfileRepository.findByCognitoSub(COGNITO_SUB)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/private/profile").with(jwt().jwt(j -> j.subject(COGNITO_SUB))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ACCOUNT_PROFILE_NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("AccountProfile not found: " + COGNITO_SUB));
  }

  @Test
  @DisplayName("updates first and last name and syncs email from the ID token")
  void updateMyAccountProfile_exists_returns200() throws Exception {
    when(accountProfileRepository.findByCognitoSub(COGNITO_SUB))
        .thenReturn(Optional.of(accountProfile()));
    when(tokenUtilityPort.validateAndDecodeIdToken(ID_TOKEN)).thenReturn(tokenClaims(COGNITO_SUB));
    when(accountProfileRepository.update(any(AccountProfile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateAccountProfileCommand command =
        UpdateAccountProfileCommand.builder().firstName("Janet").lastName("Smith").build();

    mockMvc
        .perform(
            put("/private/profile")
                .header("X-ID-Token", ID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(jwt().jwt(j -> j.subject(COGNITO_SUB))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(PROFILE_ID.toString()))
        .andExpect(jsonPath("$.cognitoSub").value(COGNITO_SUB))
        .andExpect(jsonPath("$.email").value(EMAIL))
        .andExpect(jsonPath("$.firstName").value("Janet"))
        .andExpect(jsonPath("$.lastName").value("Smith"));

    ArgumentCaptor<AccountProfile> captor = ArgumentCaptor.forClass(AccountProfile.class);
    verify(accountProfileRepository).update(captor.capture());
    verify(accountProfileRepository, never()).insert(any(AccountProfile.class));
    assertThat(captor.getValue().getFirstName()).isEqualTo("Janet");
    assertThat(captor.getValue().getLastName()).isEqualTo("Smith");
    assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
  }

  @Test
  @DisplayName("creates a profile when none exists yet for the Cognito user")
  void updateMyAccountProfile_missing_createsProfile() throws Exception {
    when(accountProfileRepository.findByCognitoSub(COGNITO_SUB)).thenReturn(Optional.empty());
    when(tokenUtilityPort.validateAndDecodeIdToken(ID_TOKEN)).thenReturn(tokenClaims(COGNITO_SUB));
    when(accountProfileRepository.insert(any(AccountProfile.class)))
        .thenAnswer(
            invocation -> {
              AccountProfile incoming = invocation.getArgument(0);
              return incoming.setId(PROFILE_ID);
            });

    UpdateAccountProfileCommand command =
        UpdateAccountProfileCommand.builder().firstName("Janet").lastName("Smith").build();

    mockMvc
        .perform(
            put("/private/profile")
                .header("X-ID-Token", ID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(jwt().jwt(j -> j.subject(COGNITO_SUB))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(PROFILE_ID.toString()))
        .andExpect(jsonPath("$.cognitoSub").value(COGNITO_SUB))
        .andExpect(jsonPath("$.email").value(EMAIL))
        .andExpect(jsonPath("$.firstName").value("Janet"))
        .andExpect(jsonPath("$.lastName").value("Smith"));

    ArgumentCaptor<AccountProfile> captor = ArgumentCaptor.forClass(AccountProfile.class);
    verify(accountProfileRepository).insert(captor.capture());
    verify(accountProfileRepository, never()).update(any(AccountProfile.class));
    assertThat(captor.getValue().getCognitoSub()).isEqualTo(COGNITO_SUB);
    assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
  }

  @Test
  @DisplayName("rejects an ID token whose sub does not match the access token user")
  void updateMyAccountProfile_subMismatch_returns401() throws Exception {
    when(tokenUtilityPort.validateAndDecodeIdToken(ID_TOKEN))
        .thenReturn(tokenClaims("other-cognito-sub"));

    UpdateAccountProfileCommand command =
        UpdateAccountProfileCommand.builder().firstName("Janet").lastName("Smith").build();

    mockMvc
        .perform(
            put("/private/profile")
                .header("X-ID-Token", ID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(jwt().jwt(j -> j.subject(COGNITO_SUB))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_ID_TOKEN"))
        .andExpect(
            jsonPath("$.message").value("ID token sub does not match the authenticated user"));

    verify(accountProfileRepository, never()).insert(any(AccountProfile.class));
    verify(accountProfileRepository, never()).update(any(AccountProfile.class));
  }

  @Test
  @DisplayName("rejects an invalid ID token from the token port")
  void updateMyAccountProfile_invalidIdToken_returns401() throws Exception {
    when(tokenUtilityPort.validateAndDecodeIdToken(anyString()))
        .thenThrow(InvalidIdTokenException.of("token_use must be id"));

    UpdateAccountProfileCommand command =
        UpdateAccountProfileCommand.builder().firstName("Janet").lastName("Smith").build();

    mockMvc
        .perform(
            put("/private/profile")
                .header("X-ID-Token", ID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(jwt().jwt(j -> j.subject(COGNITO_SUB))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_ID_TOKEN"))
        .andExpect(jsonPath("$.message").value("token_use must be id"));
  }

  private static TokenClaims tokenClaims(String sub) {
    return new TokenClaims(sub, EMAIL, List.of("USER"), Instant.parse("2026-07-14T00:00:00Z"));
  }
}
