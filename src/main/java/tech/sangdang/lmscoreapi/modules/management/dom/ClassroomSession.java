package tech.sangdang.lmscoreapi.modules.management.dom;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
  private LocalDate sessionDate;
  private LocalTime startTime;
  private LocalTime endTime;
  private UUID classroomId;
  private String name;
  private String description;
  private ClassroomSessionStatus status;
  private ClassroomSessionType type;
  private UUID generatedBy;

  public static ClassroomSession fromScheduleRecurrence(
      LocalDate sessionDate,
      LocalTime startTime,
      LocalTime endTime,
      UUID classroomId,
      String name,
      String description,
      UUID classroomScheduleRecurrenceId) {
    return new ClassroomSession()
        .setSessionDate(sessionDate)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setClassroomId(classroomId)
        .setName(name)
        .setDescription(description)
        .setStatus(ClassroomSessionStatus.OPEN)
        .setType(ClassroomSessionType.SCHEDULE)
        .setGeneratedBy(classroomScheduleRecurrenceId);
  }
}
