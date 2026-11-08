package tech.sangdang.lmscoreapi.modules.management.support;

import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CREATED_AT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.MODIFIED_AT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoice;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceLine;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoicePeriodRange;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceStatus;

public final class ClassroomInvoiceFixtures {

  public static final UUID INVOICE_ID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
  public static final long TOTAL_AMOUNT = 1_200_000L;
  public static final long PAID_AMOUNT = 1_200_000L;
  public static final String PAYMENT_REFERENCE = "TXN-12345";
  public static final String PAYMENT_NOTE = "Paid via bank transfer";
  public static final LocalDateTime APPROVED_AT = LocalDateTime.of(2026, 10, 5, 0, 0);

  private ClassroomInvoiceFixtures() {}

  public static ClassroomInvoice approvedInvoice() {
    return invoice(ClassroomInvoiceStatus.APPROVED).setApprovedAt(APPROVED_AT);
  }

  public static ClassroomInvoice invoice(ClassroomInvoiceStatus status) {
    return ClassroomInvoice.ofGenerated(
            CLASSROOM_ID,
            MEMBER_ID,
            Set.of(ClassroomInvoiceLine.ofLesson("8 lessons in October", 150_000L, 8)),
            LocalDate.of(2026, 10, 1),
            ClassroomInvoicePeriodRange.MONTH,
            "October tuition")
        .setId(INVOICE_ID)
        .setStatus(status)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }
}
