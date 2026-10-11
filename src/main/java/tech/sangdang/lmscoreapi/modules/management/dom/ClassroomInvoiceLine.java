package tech.sangdang.lmscoreapi.modules.management.dom;

import java.time.LocalDateTime;
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
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(ClassroomInvoiceLine.TABLE_NAME)
public class ClassroomInvoiceLine {
  public static final String TABLE_NAME = "classroom_invoice_line";

  private @Id UUID id;
  private @CreatedDate LocalDateTime createdDate;
  private @LastModifiedDate LocalDateTime lastModifiedDate;
  private ClassroomInvoiceLineKind kind;
  private String description;
  private Long unitPrice;
  private Integer quantity;
  private Long finalAmount = 0L;

  public static ClassroomInvoiceLine ofLesson(
      String description, Long unitPrice, Integer quantity) {
    return new ClassroomInvoiceLine()
        .setKind(ClassroomInvoiceLineKind.LESSON)
        .setDescription(description)
        .setUnitPrice(unitPrice)
        .setQuantity(quantity)
        .setFinalAmount(unitPrice * quantity);
  }
}
