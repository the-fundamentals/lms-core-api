package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceResponse;

public interface ClassroomInvoiceService {

  /**
   * Queries invoices for a classroom.
   *
   * <ul>
   *   <li>Always scoped to {@code classroomId} in addition to the filter.
   *   <li>Optional {@code memberId} filter narrows to one member.
   * </ul>
   *
   * @param classroomId classroom whose invoices to query
   * @param filter paging, sort, and field filters
   * @return matching invoices
   */
  List<ClassroomInvoiceResponse> queryInvoices(UUID classroomId, ClassroomInvoiceFilter filter);

  /**
   * Returns an invoice by id within the classroom.
   *
   * @param classroomId classroom the invoice belongs to
   * @param invoiceId invoice to load
   * @return invoice
   */
  ClassroomInvoiceResponse getInvoiceById(UUID classroomId, UUID invoiceId);

  /**
   * Marks a DRAFT invoice APPROVED.
   *
   * <ul>
   *   <li>Non-DRAFT is 400.
   * </ul>
   *
   * @param classroomId classroom the invoice belongs to
   * @param invoiceId invoice to approve
   * @return updated invoice
   */
  ClassroomInvoiceResponse approveInvoice(UUID classroomId, UUID invoiceId);
}
