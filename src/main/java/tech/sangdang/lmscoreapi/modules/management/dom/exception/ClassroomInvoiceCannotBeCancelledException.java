package tech.sangdang.lmscoreapi.modules.management.dom.exception;

import org.springframework.http.HttpStatus;
import tech.sangdang.lmscoreapi.common.exception.BusinessException;

public class ClassroomInvoiceCannotBeCancelledException extends BusinessException {
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String CODE = "CLASSROOM_INVOICE_NOT_CANCELLABLE";
  private static final String MESSAGE =
      "Invoice can only be cancelled when status is DRAFT or APPROVED";

  public ClassroomInvoiceCannotBeCancelledException() {
    super(CODE, MESSAGE, STATUS);
  }

  public static ClassroomInvoiceCannotBeCancelledException of() {
    return new ClassroomInvoiceCannotBeCancelledException();
  }
}
