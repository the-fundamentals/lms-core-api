package tech.sangdang.lmscoreapi.modules.management.dom.exception;

import org.springframework.http.HttpStatus;
import tech.sangdang.lmscoreapi.common.exception.BusinessException;

/** Thrown when date/time is changed on a non-ADHOC (e.g. SCHEDULE) session. */
public class ClassroomSessionTimeCannotBeUpdatedException extends BusinessException {
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String CODE = "CLASSROOM_SESSION_NOT_ADHOC";
  private static final String MESSAGE = "Date and time can only be changed on ADHOC sessions";

  public ClassroomSessionTimeCannotBeUpdatedException() {
    super(CODE, MESSAGE, STATUS);
  }

  public static ClassroomSessionTimeCannotBeUpdatedException of() {
    return new ClassroomSessionTimeCannotBeUpdatedException();
  }
}
