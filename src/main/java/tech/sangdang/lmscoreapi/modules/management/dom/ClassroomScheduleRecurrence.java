package tech.sangdang.lmscoreapi.modules.management.dom;

import java.time.DayOfWeek;
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
@Table(ClassroomScheduleRecurrence.TABLE_NAME)
public class ClassroomScheduleRecurrence {
  public static final String TABLE_NAME = "classroom_schedule_recurrence";

  private @Id UUID id;
  private @CreatedDate LocalDateTime createdDate;
  private @LastModifiedDate LocalDateTime lastModifiedDate;
  private LocalDateTime deletedDate;
  private UUID classroomId;
  private RecurrenceFrequency frequency;
  // JDBC enum name (MONDAY), not iCal BYDAY (MO)
  private DayOfWeek byDay;
  private LocalDate recurrenceStartDate;
  private LocalDate recurUntil;
  private LocalTime startTime;
  private LocalTime endTime;
}
