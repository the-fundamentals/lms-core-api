package tech.sangdang.lmscoreapi.modules.management.infra.jobs;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.sangdang.lmscoreapi.modules.management.app.useCase.InvoiceGeneration.ClassroomInvoiceGenerationService;

@RequiredArgsConstructor
@Component
public class ClassroomInvoiceGenerationJob {
  private final ClassroomInvoiceGenerationService classroomInvoiceGenerationService;

  /** 1st of each month 00:00 — invoices for the previous calendar month. */
  @Scheduled(cron = "${app.modules.management.invoice-generation-cron:0 0 0 1 * *}")
  public void generateInvoices() {
    LocalDateTime now = LocalDateTime.now();
    // [first day of previous month, first day of this month] — end is inclusive in the service
    LocalDateTime previousMonthStart = now.minusMonths(1).withDayOfMonth(1);
    LocalDateTime previousMonthEnd = now.with(TemporalAdjusters.firstDayOfMonth());
    classroomInvoiceGenerationService.generateInvoices(previousMonthStart, previousMonthEnd);
  }
}
