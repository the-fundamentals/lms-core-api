package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceResponse;
import tech.sangdang.lmscoreapi.generated.model.CompleteClassroomInvoiceCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomInvoiceAdhocCommand;

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
   * Creates a DRAFT ADHOC invoice for a member of the classroom.
   *
   * <ul>
   *   <li>Period is left unset.
   *   <li>Line totals and the invoice total are computed here.
   * </ul>
   *
   * @param classroomId classroom the member belongs to
   * @param command member, optional note, and at least one line
   * @return created invoice
   */
  ClassroomInvoiceResponse createAdhocInvoice(
      UUID classroomId, CreateClassroomInvoiceAdhocCommand command);

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

  /**
   * Marks an APPROVED invoice COMPLETED and records payment.
   *
   * <ul>
   *   <li>Non-APPROVED is 400.
   *   <li>{@code paidAt} is server time.
   * </ul>
   *
   * @param classroomId classroom the invoice belongs to
   * @param invoiceId invoice to complete
   * @param command paid amount and method; reference and note optional
   * @return updated invoice
   */
  ClassroomInvoiceResponse completeInvoice(
      UUID classroomId, UUID invoiceId, CompleteClassroomInvoiceCommand command);
}
