package tech.sangdang.lmscoreapi.modules.account.app.mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import tech.sangdang.lmscoreapi.generated.model.AccountProfileResponse;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;

@Mapper(componentModel = "spring")
public interface AccountProfileMapper {

  AccountProfileResponse toResponse(AccountProfile accountProfile);

  default OffsetDateTime map(LocalDateTime value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }
}
