package tech.sangdang.lmscoreapi.modules.management.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import tech.sangdang.lmscoreapi.common.DevController;
import tech.sangdang.lmscoreapi.generated.api.DevApi;
import tech.sangdang.lmscoreapi.generated.model.GenerateClassroomInvoicesCommand;
import tech.sangdang.lmscoreapi.generated.model.GenerateClassroomSessionsFromRecurrenceCommand;
import tech.sangdang.lmscoreapi.modules.management.app.useCase.InvoiceGeneration.ClassroomInvoiceGenerationService;
import tech.sangdang.lmscoreapi.modules.management.app.useCase.SessionGeneration.ClassroomSessionGenerationService;

@DevController
@RequiredArgsConstructor
public class ManagementDevController implements DevApi {

  private final ClassroomSessionGenerationService classroomSessionGenerationService;
  private final ClassroomInvoiceGenerationService classroomInvoiceGenerationService;

  @Override
  public ResponseEntity<?> generateClassroomSessionsFromRecurrence(
      GenerateClassroomSessionsFromRecurrenceCommand command) {
    classroomSessionGenerationService.generateSessionsFromRecurrence(command.getRunDate());
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<?> generateClassroomInvoices(GenerateClassroomInvoicesCommand command) {
    classroomInvoiceGenerationService.generateInvoices(
        command.getStartDateTime().toLocalDateTime(), command.getEndDateTime().toLocalDateTime());
    return ResponseEntity.noContent().build();
  }
}
