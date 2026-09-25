package tech.sangdang.lmscoreapi.modules.management.app.internal;

import lombok.experimental.UtilityClass;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleRecurrence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class RecurrenceRuleUtils {
  public static List<LocalDate> expandRecurrenceRule(
      ClassroomScheduleRecurrence recurrence, LocalDate startDate, LocalDate endDate) {
    // clamp down start and end date
    LocalDate effectiveStart =
        startDate.isAfter(recurrence.getRecurrenceStartDate())
            ? startDate
            : recurrence.getRecurrenceStartDate();
    LocalDate effectiveEnd =
        (recurrence.getRecurUntil() != null && recurrence.getRecurUntil().isBefore(endDate))
            ? recurrence.getRecurUntil()
            : endDate;

    return switch (recurrence.getFrequency()) {
      case WEEKLY -> expandWeekly(recurrence, effectiveStart, effectiveEnd);
      case null -> throw new IllegalArgumentException("Unknown Frequency");
    };
  }

  private static List<LocalDate> expandWeekly(
      ClassroomScheduleRecurrence recurrence, LocalDate effectiveStart, LocalDate effectiveEnd) {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate cursor = effectiveStart;

    while (cursor.getDayOfWeek() != recurrence.getByDay()) {
      cursor = cursor.plusDays(1);
    }

    while (cursor.isBefore(effectiveEnd) || cursor.isEqual(effectiveEnd)) {
      dates.add(cursor);
      cursor = cursor.plusDays(7);
    }

    return dates;
  }
}
