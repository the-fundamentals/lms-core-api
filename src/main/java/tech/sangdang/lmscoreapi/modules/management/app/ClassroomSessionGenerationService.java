package tech.sangdang.lmscoreapi.modules.management.app;

import java.time.LocalDate;
import java.util.UUID;

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

  /**
   * Inserts generated sessions for one classroom in {@code [startDate, endDate]}.
   *
   * <ul>
   *   <li>Classroom must be ACTIVE; otherwise not found.
   *   <li>Existing generated rows for the same classroom, recurrence, and date are skipped.
   * </ul>
   *
   * @param classroomId classroom to generate for
   * @param startDate inclusive window start
   * @param endDate inclusive window end
   */
  void generateSessionsForClassroom(UUID classroomId, LocalDate startDate, LocalDate endDate);
}
