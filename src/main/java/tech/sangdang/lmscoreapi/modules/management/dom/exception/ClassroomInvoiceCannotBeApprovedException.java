package tech.sangdang.lmscoreapi.modules.management.dom.exception;

import org.springframework.http.HttpStatus;
import tech.sangdang.lmscoreapi.common.exception.BusinessException;

public class ClassroomInvoiceCannotBeApprovedException extends BusinessException {
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String CODE = "CLASSROOM_INVOICE_NOT_DRAFT";
  private static final String MESSAGE = "Invoice can only be approved when status is DRAFT";

  public ClassroomInvoiceCannotBeApprovedException() {
    super(CODE, MESSAGE, STATUS);
  }

  public static ClassroomInvoiceCannotBeApprovedException of() {
    return new ClassroomInvoiceCannotBeApprovedException();
  }
}
