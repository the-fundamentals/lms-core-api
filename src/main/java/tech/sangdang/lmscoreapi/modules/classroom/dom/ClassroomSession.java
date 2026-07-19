package tech.sangdang.lmscoreapi.modules.classroom.dom;

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
@Table(ClassroomSession.TABLE_NAME)
public class ClassroomSession {
  public static final String TABLE_NAME = "classroom_session";

  private @Id UUID id;
  private @CreatedDate LocalDateTime createdDate;
  private @LastModifiedDate LocalDateTime lastModifiedDate;
  private LocalDateTime sessionDate;
  private UUID classroomId;
}
