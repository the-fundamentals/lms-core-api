package tech.sangdang.lmscoreapi.modules.management.dom;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = ClassroomMemberPaymentPlan.TABLE_NAME)
public class ClassroomMemberPaymentPlan {
  public static final String TABLE_NAME = "classroom_member_payment_plan";

  private @Id UUID id;
  private @CreatedDate Instant createdDate;
  private @LastModifiedDate Instant lastModifiedDate;
  private UUID classroomMemberId;
  private PaymentPlanType type;
  private Long amount;
  private String currency;
  private Boolean isCurrent;
  private Instant replacedAt;
}
