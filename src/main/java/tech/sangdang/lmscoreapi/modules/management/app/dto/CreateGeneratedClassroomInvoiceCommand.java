package tech.sangdang.lmscoreapi.modules.management.app.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoicePeriodRange;

/**
 * Internal command to create a GENERATED classroom invoice (no HTTP endpoint).
 *
 * @param memberId classroom member billed
 * @param periodStart period start date
 * @param periodRange period range (e.g. MONTH)
 * @param note optional note
 * @param lines lesson lines; may be empty
 */
public record CreateGeneratedClassroomInvoiceCommand(
    UUID memberId,
    LocalDate periodStart,
    ClassroomInvoicePeriodRange periodRange,
    String note,
    List<Line> lines) {

  public record Line(String description, Long unitPrice, Integer quantity) {}
}
