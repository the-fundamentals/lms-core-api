package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleRecurrenceResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleRecurrenceCommand;

public interface ClassroomScheduleRecurrenceService {

  /**
   * Creates a weekly RRULE recurrence for the classroom.
   *
   * <ul>
   *   <li>Non-weekly or invalid RRULE is 400; does not expand occurrences.
   * </ul>
   *
   * @param classroomId classroom the recurrence belongs to
   * @param command RRULE, start date, and daily times
   * @return created recurrence
   */
  ClassroomScheduleRecurrenceResponse createClassroomScheduleRecurrence(
      UUID classroomId, CreateClassroomScheduleRecurrenceCommand command);

  /**
   * Lists non-deleted recurrences for the classroom.
   *
   * @param classroomId classroom whose recurrences to load
   * @return active recurrences
   */
  List<ClassroomScheduleRecurrenceResponse> getAllClassroomScheduleRecurrences(UUID classroomId);

  /**
   * Soft-deletes a recurrence (sets {@code deletedDate}).
   *
   * @param classroomId classroom the recurrence belongs to
   * @param scheduleId recurrence to delete
   */
  void deleteClassroomScheduleRecurrence(UUID classroomId, UUID scheduleId);
}
