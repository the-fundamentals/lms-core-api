package tech.sangdang.lmscoreapi.modules.management.api;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

  /**
   * BAND-AID: invoice generate accepts date-time; civil billing windows should be {@code date}
   * only. Until that API change, interpret instants in GMT+7 so FE Z midnights don't shift a day.
   */
  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

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
    // BAND-AID: see BUSINESS_ZONE. Prefer LocalDate generate inputs later.
    classroomInvoiceGenerationService.generateInvoices(
        toBusinessLocal(command.getStartDateTime()), toBusinessLocal(command.getEndDateTime()));
    return ResponseEntity.noContent().build();
  }

  private static LocalDateTime toBusinessLocal(OffsetDateTime value) {
    return value.atZoneSameInstant(BUSINESS_ZONE).toLocalDateTime();
  }
}
