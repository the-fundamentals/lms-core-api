package tech.sangdang.lmscoreapi.modules.management.dom.exception;

import org.springframework.http.HttpStatus;
import tech.sangdang.lmscoreapi.common.exception.BusinessException;

public class InvalidRecurrenceRuleException extends BusinessException {
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String CODE = "INVALID_RRULE";

  public InvalidRecurrenceRuleException(String message) {
    super(CODE, message, STATUS);
  }

  public static InvalidRecurrenceRuleException of(String message) {
    return new InvalidRecurrenceRuleException(message);
  }
}
