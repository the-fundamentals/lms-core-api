package tech.sangdang.lmscoreapi.modules.management.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomInvoicesApi;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceFilter;
import tech.sangdang.lmscoreapi.generated.model.CompleteClassroomInvoiceCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomInvoiceService;

@RestController
@RequiredArgsConstructor
public class ClassroomInvoiceController implements ClassroomInvoicesApi {

  private final ClassroomInvoiceService classroomInvoiceService;

  @Override
  public ResponseEntity<?> queryClassroomInvoices(
      @NonNull UUID classroomId, @NonNull ClassroomInvoiceFilter classroomInvoiceFilter) {
    return ResponseEntity.ok(
        classroomInvoiceService.queryInvoices(classroomId, classroomInvoiceFilter));
  }

  @Override
  public ResponseEntity<?> getClassroomInvoiceById(
      @NonNull UUID classroomId, @NonNull UUID invoiceId) {
    return ResponseEntity.ok(classroomInvoiceService.getInvoiceById(classroomId, invoiceId));
  }

  @Override
  public ResponseEntity<?> approveClassroomInvoice(
      @NonNull UUID classroomId, @NonNull UUID invoiceId) {
    return ResponseEntity.ok(classroomInvoiceService.approveInvoice(classroomId, invoiceId));
  }

  @Override
  public ResponseEntity<?> completeClassroomInvoice(
      @NonNull UUID classroomId,
      @NonNull UUID invoiceId,
      @NonNull CompleteClassroomInvoiceCommand completeClassroomInvoiceCommand) {
    return ResponseEntity.ok(
        classroomInvoiceService.completeInvoice(
            classroomId, invoiceId, completeClassroomInvoiceCommand));
  }
}
