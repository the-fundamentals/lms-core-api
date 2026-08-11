package tech.sangdang.lmscoreapi.common.exception;

import org.springframework.http.HttpStatus;

public class GenericBadRequestException extends BusinessException {

  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

  public GenericBadRequestException(String code, String message) {
    super(code, message, STATUS);
  }

  public static GenericBadRequestException of(String code, String message) {
    return new GenericBadRequestException(code, message);
  }
}
