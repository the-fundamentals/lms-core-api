package tech.sangdang.lmscoreapi.common.exception;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiErrorResponse {

  String code;
  String message;
  int status;
  OffsetDateTime timestamp;
  String path;
}
