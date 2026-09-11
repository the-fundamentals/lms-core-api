package tech.sangdang.lmscoreapi.modules.management.support;

import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;

import java.time.Instant;
import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberPaymentPlan;
import tech.sangdang.lmscoreapi.modules.management.dom.PaymentPlanType;

public final class ClassroomMemberPaymentPlanFixtures {

  public static final UUID PAYMENT_PLAN_ID =
      UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
  public static final UUID PREVIOUS_PAYMENT_PLAN_ID =
      UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
  public static final long AMOUNT = 150_000L;
  public static final String CURRENCY = "VND";
  public static final Instant CREATED_AT = Instant.parse("2026-07-14T00:00:00Z");
  public static final Instant MODIFIED_AT = Instant.parse("2026-07-14T00:00:00Z");

  private ClassroomMemberPaymentPlanFixtures() {}

  public static ClassroomMemberPaymentPlan currentPaymentPlan() {
    return paymentPlan(PAYMENT_PLAN_ID, MEMBER_ID, true, null, Instant.now());
  }

  public static ClassroomMemberPaymentPlan paymentPlan(
      UUID id, UUID classroomMemberId, boolean isCurrent, Instant replacedAt, Instant createdDate) {
    return new ClassroomMemberPaymentPlan()
        .setId(id)
        .setClassroomMemberId(classroomMemberId)
        .setType(PaymentPlanType.PER_SESSION)
        .setAmount(AMOUNT)
        .setCurrency(CURRENCY)
        .setIsCurrent(isCurrent)
        .setReplacedAt(replacedAt)
        .setCreatedDate(createdDate)
        .setLastModifiedDate(createdDate);
  }
}
