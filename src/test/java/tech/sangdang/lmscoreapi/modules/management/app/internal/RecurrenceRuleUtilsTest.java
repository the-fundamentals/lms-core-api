package tech.sangdang.lmscoreapi.modules.management.app.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.classroomScheduleRecurrence;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleRecurrence;

@DisplayName("Recurrence rule expansion")
class RecurrenceRuleUtilsTest {

  private static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
  private static final LocalDate NOV_30 = LocalDate.of(2026, 11, 30);

  @Test
  @DisplayName("expands weekly Mondays across November 2026")
  void expandRecurrenceRule_novemberMondays_returnsFiveDates() {
    List<LocalDate> dates =
        RecurrenceRuleUtils.expandRecurrenceRule(classroomScheduleRecurrence(), NOV_1, NOV_30);

    assertThat(dates)
        .containsExactly(
            LocalDate.of(2026, 11, 2),
            LocalDate.of(2026, 11, 9),
            LocalDate.of(2026, 11, 16),
            LocalDate.of(2026, 11, 23),
            LocalDate.of(2026, 11, 30));
  }

  @Test
  @DisplayName("snaps a mid-week start to the next by-day then weekly")
  void expandRecurrenceRule_thursdayStart_snapsToMonday() {
    ClassroomScheduleRecurrence recurrence =
        classroomScheduleRecurrence().setRecurrenceStartDate(LocalDate.of(2026, 11, 5));

    List<LocalDate> dates =
        RecurrenceRuleUtils.expandRecurrenceRule(recurrence, NOV_1, NOV_30);

    assertThat(dates)
        .containsExactly(
            LocalDate.of(2026, 11, 9),
            LocalDate.of(2026, 11, 16),
            LocalDate.of(2026, 11, 23),
            LocalDate.of(2026, 11, 30));
  }

  @Test
  @DisplayName("includes an end date that falls on the by-day")
  void expandRecurrenceRule_endOnMonday_includesEnd() {
    List<LocalDate> dates =
        RecurrenceRuleUtils.expandRecurrenceRule(
            classroomScheduleRecurrence(), NOV_1, LocalDate.of(2026, 11, 16));

    assertThat(dates)
        .containsExactly(
            LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 16));
  }

  @Test
  @DisplayName("clamps the window start to the recurrence start date")
  void expandRecurrenceRule_startAfterWindow_clampsToRecurrenceStart() {
    ClassroomScheduleRecurrence recurrence =
        classroomScheduleRecurrence().setRecurrenceStartDate(LocalDate.of(2026, 11, 11));

    List<LocalDate> dates =
        RecurrenceRuleUtils.expandRecurrenceRule(recurrence, NOV_1, NOV_30);

    assertThat(dates)
        .containsExactly(
            LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 30));
  }

  @Test
  @DisplayName("clamps the window end to recurUntil")
  void expandRecurrenceRule_recurUntilBeforeWindowEnd_stopsAtUntil() {
    ClassroomScheduleRecurrence recurrence =
        classroomScheduleRecurrence().setRecurUntil(LocalDate.of(2026, 11, 16));

    List<LocalDate> dates =
        RecurrenceRuleUtils.expandRecurrenceRule(recurrence, NOV_1, NOV_30);

    assertThat(dates)
        .containsExactly(
            LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 16));
  }

  @Test
  @DisplayName("returns no dates when the first by-day is after the window")
  void expandRecurrenceRule_firstMondayAfterOctober_empty() {
    ClassroomScheduleRecurrence recurrence =
        classroomScheduleRecurrence().setRecurrenceStartDate(LocalDate.of(2026, 10, 30));

    List<LocalDate> dates =
        RecurrenceRuleUtils.expandRecurrenceRule(
            recurrence, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

    assertThat(dates).isEmpty();
  }
}
