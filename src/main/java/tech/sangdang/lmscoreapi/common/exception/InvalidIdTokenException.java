package tech.sangdang.lmscoreapi.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidIdTokenException extends BusinessException {

  private static final String CODE = "INVALID_ID_TOKEN";
  private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

  public InvalidIdTokenException(String message) {
    super(CODE, message, STATUS);
  }

  public InvalidIdTokenException(String message, Throwable cause) {
    super(CODE, message, STATUS, cause);
  }

  public static InvalidIdTokenException of(String message) {
    return new InvalidIdTokenException(message);
  }

  public static InvalidIdTokenException of(String message, Throwable cause) {
    return new InvalidIdTokenException(message, cause);
  }
}
