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
import tech.sangdang.lmscoreapi.modules.management.dom.exception.ClassroomSessionCannotBeUpdatedException;

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

  /**
   * Builds an adhoc session: status OPEN, type ADHOC.
   *
   * @param sessionDate calendar date
   * @param startTime session start
   * @param endTime session end
   * @param classroomId classroom the session belongs to
   * @param name optional display name
   * @param description optional description
   */
  public static ClassroomSession fromAdhoc(
      LocalDate sessionDate,
      LocalTime startTime,
      LocalTime endTime,
      UUID classroomId,
      String name,
      String description) {
    return new ClassroomSession()
        .setSessionDate(sessionDate)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setClassroomId(classroomId)
        .setName(name)
        .setDescription(description)
        .setStatus(ClassroomSessionStatus.OPEN)
        .setType(ClassroomSessionType.ADHOC);
  }

  /**
   * Builds a generated schedule session: status OPEN, type SCHEDULE.
   *
   * @param sessionDate calendar date
   * @param startTime session start
   * @param endTime session end
   * @param classroomId classroom the session belongs to
   * @param name display name
   * @param description description
   * @param classroomScheduleRecurrenceId recurrence that produced this row
   */
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

  /**
   * Whether the session can be mutated (attendance changes or completing).
   *
   * <ul>
   *   <li>True only when status is OPEN.
   * </ul>
   */
  public boolean canBeUpdated() {
    return status == ClassroomSessionStatus.OPEN;
  }

  /**
   * Rejects mutations unless {@link #canBeUpdated()}.
   *
   * @throws ClassroomSessionCannotBeUpdatedException when status is COMPLETED or CANCELLED
   */
  public void requireCanBeUpdated() {
    if (!canBeUpdated()) {
      throw ClassroomSessionCannotBeUpdatedException.of();
    }
  }

  /**
   * Sets status to COMPLETED.
   *
   * <ul>
   *   <li>Only from OPEN.
   * </ul>
   *
   * @throws ClassroomSessionCannotBeUpdatedException when status is not OPEN
   */
  public void complete() {
    requireCanBeUpdated();
    this.status = ClassroomSessionStatus.COMPLETED;
  }
}
