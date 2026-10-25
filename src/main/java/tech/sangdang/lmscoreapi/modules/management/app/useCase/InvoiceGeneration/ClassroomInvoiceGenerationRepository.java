package tech.sangdang.lmscoreapi.modules.management.app.useCase.InvoiceGeneration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoice;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceLine;

@Repository
@RequiredArgsConstructor
public class ClassroomInvoiceGenerationRepository {
  private final NamedParameterJdbcTemplate template;

  /**
   * Inserts GENERATED invoices and their lines; skips existing member/period rows.
   *
   * <ul>
   *   <li>Conflict target: {@code unique_generated_invoice_member_period}.
   *   <li>Lines are written only for newly inserted invoices.
   * </ul>
   *
   * @param invoices invoices to insert (null/empty is a no-op)
   */
  public void insertAllIgnoringDuplicates(List<ClassroomInvoice> invoices) {
    if (invoices == null) {
      return;
    }
    invoices = invoices.stream().filter(Objects::nonNull).toList();
    if (invoices.isEmpty()) {
      return;
    }

    // Assign ids up front so line rows can reference them after a successful invoice insert.
    List<UUID> invoiceIds = new ArrayList<>(invoices.size());
    for (ClassroomInvoice invoice : invoices) {
      UUID id = invoice.getId() != null ? invoice.getId() : UUID.randomUUID();
      invoice.setId(id);
      invoiceIds.add(id);
    }

    // Ad-hoc SQL: Criteria cannot express ON CONFLICT on partial unique index
    // unique_generated_invoice_member_period (member_id, period_range, period_start) WHERE type =
    // 'GENERATED'
    String invoiceSql =
        """
        INSERT INTO classroom_invoice (
          id, classroom_id, member_id, status, type,
          period_start, period_range, total_amount, note
        ) VALUES (
          :id, :classroomId, :memberId, :status, :type,
          :periodStart, :periodRange, :totalAmount, :note
        )
        ON CONFLICT (member_id, period_range, period_start) WHERE type = 'GENERATED'
        DO NOTHING
        """;

    SqlParameterSource[] invoiceArgs =
        invoices.stream()
            .map(
                invoice ->
                    new MapSqlParameterSource()
                        .addValue("id", invoice.getId())
                        .addValue("classroomId", invoice.getClassroomId())
                        .addValue("memberId", invoice.getMemberId())
                        .addValue("status", invoice.getStatus().name())
                        .addValue("type", invoice.getType().name())
                        .addValue("periodStart", invoice.getPeriodStart())
                        .addValue(
                            "periodRange",
                            invoice.getPeriodRange() == null
                                ? null
                                : invoice.getPeriodRange().name())
                        .addValue("totalAmount", invoice.getTotalAmount())
                        .addValue("note", invoice.getNote()))
            .toArray(SqlParameterSource[]::new);

    int[] inserted = template.batchUpdate(invoiceSql, invoiceArgs);

    String lineSql =
        """
        INSERT INTO classroom_invoice_line (
          invoice_id, kind, description, unit_price, quantity, final_amount
        ) VALUES (
          :invoiceId, :kind, :description, :unitPrice, :quantity, :finalAmount
        )
        """;

    List<SqlParameterSource> lineArgs = new ArrayList<>();
    for (int i = 0; i < invoices.size(); i++) {
      if (inserted[i] == 0) {
        continue; // conflict — invoice already existed; do not add lines
      }
      Set<ClassroomInvoiceLine> lines = invoices.get(i).getLines();
      if (lines == null || lines.isEmpty()) {
        continue;
      }
      UUID invoiceId = invoiceIds.get(i);
      for (ClassroomInvoiceLine line : lines) {
        lineArgs.add(
            new MapSqlParameterSource()
                .addValue("invoiceId", invoiceId)
                .addValue("kind", line.getKind().name())
                .addValue("description", line.getDescription())
                .addValue("unitPrice", line.getUnitPrice())
                .addValue("quantity", line.getQuantity())
                .addValue("finalAmount", line.getFinalAmount()));
      }
    }

    if (!lineArgs.isEmpty()) {
      template.batchUpdate(lineSql, lineArgs.toArray(SqlParameterSource[]::new));
    }
  }
}
