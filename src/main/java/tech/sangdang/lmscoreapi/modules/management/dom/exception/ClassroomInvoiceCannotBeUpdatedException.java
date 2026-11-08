package tech.sangdang.lmscoreapi.modules.management.dom.exception;

import org.springframework.http.HttpStatus;
import tech.sangdang.lmscoreapi.common.exception.BusinessException;

public class ClassroomInvoiceCannotBeUpdatedException extends BusinessException {
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String CODE = "CLASSROOM_INVOICE_NOT_EDITABLE";
  private static final String MESSAGE = "Invoice can only be updated when status is DRAFT";

  public ClassroomInvoiceCannotBeUpdatedException() {
    super(CODE, MESSAGE, STATUS);
  }

  public static ClassroomInvoiceCannotBeUpdatedException of() {
    return new ClassroomInvoiceCannotBeUpdatedException();
  }
}
