package tech.sangdang.lmscoreapi.modules.account.app.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.generated.model.AccountFilter;
import tech.sangdang.lmscoreapi.generated.model.AccountProfileResponse;
import tech.sangdang.lmscoreapi.modules.account.app.AccountService;
import tech.sangdang.lmscoreapi.modules.account.app.mappers.AccountProfileMapper;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final AccountProfileRepository accountProfileRepository;
  private final AccountProfileMapper accountProfileMapper;

  @Override
  @Transactional(readOnly = true)
  public List<AccountProfileResponse> queryAccounts(AccountFilter filter) {
    return accountProfileRepository
        .query(accountProfileMapper.toBaseQuery(filter))
        .map(accountProfileMapper::toResponse)
        .toList();
  }
}
