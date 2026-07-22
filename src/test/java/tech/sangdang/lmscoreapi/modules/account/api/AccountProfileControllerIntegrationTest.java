package tech.sangdang.lmscoreapi.modules.account.api;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.COGNITO_SUB;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.EMAIL;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.FIRST_NAME;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.LAST_NAME;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.PROFILE_ID;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.accountProfile;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.modules.account.app.impl.AccountProfileServiceImpl;
import tech.sangdang.lmscoreapi.modules.account.app.mappers.AccountProfileMapperImpl;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;

@WebMvcTest(controllers = AccountProfileController.class)
@Import({
  GlobalExceptionHandler.class,
  AccountProfileServiceImpl.class,
  AccountProfileMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Account profile")
class AccountProfileControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AccountProfileRepository accountProfileRepository;

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
}
