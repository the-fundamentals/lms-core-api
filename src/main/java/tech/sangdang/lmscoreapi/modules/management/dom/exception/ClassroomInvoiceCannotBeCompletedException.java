package tech.sangdang.lmscoreapi.modules.management.dom.exception;

import org.springframework.http.HttpStatus;
import tech.sangdang.lmscoreapi.common.exception.BusinessException;

public class ClassroomInvoiceCannotBeCompletedException extends BusinessException {
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String CODE = "CLASSROOM_INVOICE_NOT_APPROVED";
  private static final String MESSAGE = "Invoice can only be completed when status is APPROVED";

  public ClassroomInvoiceCannotBeCompletedException() {
    super(CODE, MESSAGE, STATUS);
  }

  public static ClassroomInvoiceCannotBeCompletedException of() {
    return new ClassroomInvoiceCannotBeCompletedException();
  }
}
