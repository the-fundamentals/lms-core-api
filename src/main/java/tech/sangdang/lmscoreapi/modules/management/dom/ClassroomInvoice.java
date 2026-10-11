package tech.sangdang.lmscoreapi.modules.management.dom;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import tech.sangdang.lmscoreapi.modules.management.dom.exception.ClassroomInvoiceCannotBeApprovedException;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(ClassroomInvoice.TABLE_NAME)
public class ClassroomInvoice {
  public static final String TABLE_NAME = "classroom_invoice";

  private @Id UUID id;
  private @CreatedDate LocalDateTime createdDate;
  private @LastModifiedDate LocalDateTime lastModifiedDate;

  /** FK to {@code classroom.id} (column {@code classroom_id}). */
  private UUID classroomId;

  /** FK to {@code classroom_member.id} (column {@code member_id}). */
  private UUID memberId;

  private ClassroomInvoiceStatus status;
  private ClassroomInvoiceType type;
  private LocalDate periodStart;
  private ClassroomInvoicePeriodRange periodRange;
  private Long totalAmount;
  private String note;
  private LocalDateTime approvedAt;
  private LocalDateTime paidAt;
  private Long paidAmount;
  private ClassroomInvoicePaymentMethod paymentMethod;
  private String paymentReference;
  private String paymentNote;

  /** Aggregate children. {@code invoice_id} is owned by SD JDBC (not a field on the line). */
  @MappedCollection(idColumn = "invoice_id")
  private Set<ClassroomInvoiceLine> lines = new HashSet<>();

  // ===============================
  // Invoice Creation
  // ===============================

  public static ClassroomInvoice ofGenerated(
      UUID classroomId,
      UUID memberId,
      Set<ClassroomInvoiceLine> lines,
      LocalDate periodStart,
      ClassroomInvoicePeriodRange periodRange,
      String note) {
    Set<ClassroomInvoiceLine> invoiceLines = lines == null ? new HashSet<>() : lines;
    return new ClassroomInvoice()
        .setClassroomId(classroomId)
        .setMemberId(memberId)
        .setStatus(ClassroomInvoiceStatus.DRAFT)
        .setType(ClassroomInvoiceType.GENERATED)
        .setPeriodStart(periodStart)
        .setPeriodRange(periodRange)
        .setNote(note)
        .setTotalAmount(ClassroomInvoice.calculateTotalAmount(invoiceLines))
        .setLines(invoiceLines);
  }

  public static ClassroomInvoice ofAdhoc() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  // ===============================
  // Invoice Approval
  // ===============================

  public void approve() {
    requireCanBeApproved();
    status = ClassroomInvoiceStatus.APPROVED;
    approvedAt = LocalDateTime.now();
  }

  public boolean canBeApproved() {
    return status == ClassroomInvoiceStatus.DRAFT;
  }

  public void requireCanBeApproved() {
    if (!canBeApproved()) {
      throw ClassroomInvoiceCannotBeApprovedException.of();
    }
  }

  // ===============================
  // Invoice Completion
  // ===============================

  public void complete(
      Long paidAmount,
      ClassroomInvoicePaymentMethod paymentMethod,
      String paymentReference,
      String paymentNote) {
    requireCanBeCompleted();
    this.status = ClassroomInvoiceStatus.COMPLETED;
    this.paidAt = LocalDateTime.now();
    this.paymentReference = paymentReference;
    this.paymentNote = paymentNote;
    this.paidAmount = paidAmount;
    this.paymentMethod = paymentMethod;
  }

  public boolean canBeCompleted() {
    return status == ClassroomInvoiceStatus.APPROVED;
  }

  public void requireCanBeCompleted() {
    if (!canBeCompleted()) {
      throw new IllegalStateException("Cannot complete invoice");
    }
  }

  // ===============================
  // Utilities
  // ===============================

  public static Long calculateTotalAmount(Set<ClassroomInvoiceLine> items) {
    return items.stream().mapToLong(ClassroomInvoiceLine::getFinalAmount).sum();
  }
}
