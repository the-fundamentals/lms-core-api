package tech.sangdang.lmscoreapi.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiErrorResponse> handleBusinessException(
      BusinessException exception, HttpServletRequest request) {
    HttpStatus status = exception.getStatus();
    ApiErrorResponse body =
        ApiErrorResponse.builder()
            .code(exception.getCode())
            .message(exception.getMessage())
            .status(status.value())
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .build();
    return ResponseEntity.status(status).body(body);
  }
}
