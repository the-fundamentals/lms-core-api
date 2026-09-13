package tech.sangdang.lmscoreapi.modules.management.dom;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.transform.recurrence.Frequency;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import tech.sangdang.lmscoreapi.common.utility.RRuleValidation;
import tech.sangdang.lmscoreapi.modules.management.dom.exception.InvalidRecurrenceRuleException;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(ClassroomSchedule.TABLE_NAME)
public class ClassroomSchedule {
  public static final String TABLE_NAME = "classroom_schedule";

  private @Id UUID id;
  private @CreatedDate LocalDateTime createdDate;
  private @LastModifiedDate LocalDateTime lastModifiedDate;
  private LocalDateTime deletedDate;
  private String scheduleRule;
  private LocalTime startTime;
  private LocalTime endTime;
  private UUID classroomId;

  public ClassroomSchedule setScheduleRule(String scheduleRule) {
    RRule<Temporal> rrule = RRuleValidation.validateRecurrenceRule(scheduleRule);

    if (rrule == null) {
      throw new InvalidRecurrenceRuleException("Invalid Recurrence Rule format");
    }

    // only allow weekly recursion
    Frequency frequency = rrule.getRecur().getFrequency();
    if (frequency == null || !frequency.equals(Frequency.WEEKLY)) {
      throw new InvalidRecurrenceRuleException("Recurrence Rules can only be weekly.");
    }

    this.scheduleRule = scheduleRule;
    return this;
  }
}
