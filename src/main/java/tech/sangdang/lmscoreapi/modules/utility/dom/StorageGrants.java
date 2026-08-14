package tech.sangdang.lmscoreapi.modules.utility.dom;

import java.time.LocalDateTime;
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
@Table(StorageGrants.TABLE_NAME)
public class StorageGrants {
  public static final String TABLE_NAME = "storage_grants";

  private @Id Long id; // auto increment
  private @CreatedDate LocalDateTime createdDate;
  private @LastModifiedDate LocalDateTime lastModifiedDate;

  private UUID ownerId;
  private String objectKey;
  private String objectBucket;
}
