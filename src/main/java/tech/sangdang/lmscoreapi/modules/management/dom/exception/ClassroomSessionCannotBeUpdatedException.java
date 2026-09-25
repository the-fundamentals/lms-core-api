package tech.sangdang.lmscoreapi.modules.management.dom.exception;

import org.springframework.http.HttpStatus;
import tech.sangdang.lmscoreapi.common.exception.BusinessException;

public class ClassroomSessionCannotBeUpdatedException extends BusinessException {
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String CODE = "CLASSROOM_SESSION_NOT_OPEN";
  private static final String MESSAGE = "Session can only be changed when status is OPEN";

  public ClassroomSessionCannotBeUpdatedException() {
    super(CODE, MESSAGE, STATUS);
  }

  public static ClassroomSessionCannotBeUpdatedException of() {
    return new ClassroomSessionCannotBeUpdatedException();
  }
}
