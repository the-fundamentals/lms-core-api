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

  /**
   * 1st of each month 00:00 — one GENERATED draft invoice per active student for the previous
   * calendar month.
   *
   * <ul>
   *   <li>Cron {@code app.modules.management.invoice-generation-cron}, default {@code 0 0 0 1 * *}.
   *   <li>Window is day 1 of the previous month through day 1 of this month, at the run's clock
   *       time. The service includes both ends ({@code >=} start, {@code <=} end).
   *   <li>Loads active classrooms ({@code ACTIVE} and {@code isActive}), then their active {@code
   *       STUDENT} members. Classrooms, members, and attendances each use {@code fetchAll} (max
   *       10k). No active classrooms or students: logs and returns.
   *   <li>Counts {@code ATTENDED} rows in the window ({@code hasAttended}). Zero attendances still
   *       get an invoice; quantity is 0.
   *   <li>One {@code LESSON} line. Unit price is the member's current plan ({@code effective_to}
   *       null), or 0 if none. {@code periodRange} MONTH; {@code periodStart} is the window start
   *       date. Status stays {@code DRAFT}.
   *   <li>Insert skips an existing GENERATED invoice for the same member and period (partial unique
   *       index). Does not create ad-hoc invoices, and does not approve or complete.
   * </ul>
   */
  @Scheduled(cron = "${app.modules.management.invoice-generation-cron:0 0 0 1 * *}")
  public void generateInvoices() {
    LocalDateTime now = LocalDateTime.now();
    // [first day of previous month, first day of this month] — end is inclusive in the service
    LocalDateTime previousMonthStart = now.minusMonths(1).withDayOfMonth(1);
    LocalDateTime previousMonthEnd = now.with(TemporalAdjusters.firstDayOfMonth());
    classroomInvoiceGenerationService.generateInvoices(previousMonthStart, previousMonthEnd);
  }
}
