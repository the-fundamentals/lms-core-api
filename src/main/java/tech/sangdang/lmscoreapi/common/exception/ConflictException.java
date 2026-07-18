package tech.sangdang.lmscoreapi.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {

  private static final HttpStatus STATUS = HttpStatus.CONFLICT;

  public ConflictException(String code, String message) {
    super(code, message, STATUS);
  }

  public static ConflictException of(String code, String message) {
    return new ConflictException(code, message);
  }
}
