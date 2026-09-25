package tech.sangdang.lmscoreapi.modules.management.app;

import java.time.LocalDate;

public interface ClassroomSessionGenerationService {

  /**
   * Inserts generated sessions for the calendar month after {@code runDate}.
   *
   * <ul>
   *   <li>Window is the next calendar month (first day through last day, inclusive).
   *   <li>Only active classrooms; existing generated rows for the same classroom, recurrence, and
   *       date are skipped.
   * </ul>
   *
   * @param runDate date this ran; sessions are for the following month
   */
  void generateSessionsFromRecurrence(LocalDate runDate);
}
