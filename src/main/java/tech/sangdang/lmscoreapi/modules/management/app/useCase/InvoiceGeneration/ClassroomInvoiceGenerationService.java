package tech.sangdang.lmscoreapi.modules.management.app.useCase.InvoiceGeneration;

import java.time.LocalDateTime;

public interface ClassroomInvoiceGenerationService {

  /**
   * Creates GENERATED draft invoices for active students from attendance in the window.
   *
   * <ul>
   *   <li>Includes members with zero attendances (qty 0).
   *   <li>Unit price is the member's current plan ({@code effective_to} null); 0 if none.
   *   <li>Existing GENERATED invoices for the same member/period are skipped.
   *   <li>MVP: each load uses {@code fetchAll} (max 10k rows).
   * </ul>
   *
   * @param startDateTime inclusive attendance window start
   * @param endDateTime inclusive attendance window end
   */
  void generateInvoices(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
