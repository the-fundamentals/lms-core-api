package tech.sangdang.lmscoreapi.modules.account.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.helpers.SecurityTestSupport.adminJwt;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.AVATAR_KEY;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.COGNITO_SUB;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.EMAIL;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.FIRST_NAME;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.LAST_NAME;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.PROFILE_ID;
import static tech.sangdang.lmscoreapi.modules.account.support.AccountProfileFixtures.accountProfile;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.AccountFilter;
import tech.sangdang.lmscoreapi.modules.account.app.impl.AccountServiceImpl;
import tech.sangdang.lmscoreapi.modules.account.app.mappers.AccountProfileMapperImpl;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = AccountController.class)
@Import({
  GlobalExceptionHandler.class,
  AccountServiceImpl.class,
  AccountProfileMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Account management")
class AccountControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private AccountProfileRepository accountProfileRepository;

  @Test
  @DisplayName("queries accounts")
  void getAllAccounts_returns200() throws Exception {
    when(accountProfileRepository.query(any(BaseQuery.class)))
        .thenReturn(Stream.of(accountProfile()));

    AccountFilter filter = AccountFilter.builder().build();

    mockMvc
        .perform(
            post("/admin/account/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(PROFILE_ID.toString()))
        .andExpect(jsonPath("$[0].cognitoSub").value(COGNITO_SUB))
        .andExpect(jsonPath("$[0].email").value(EMAIL))
        .andExpect(jsonPath("$[0].firstName").value(FIRST_NAME))
        .andExpect(jsonPath("$[0].lastName").value(LAST_NAME))
        .andExpect(jsonPath("$[0].avatarKey").value(AVATAR_KEY));

    verify(accountProfileRepository).query(any(BaseQuery.class));
  }
}
