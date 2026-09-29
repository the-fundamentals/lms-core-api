package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleRecurrenceResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleRecurrenceCommand;

public interface ClassroomScheduleRecurrenceService {

  /**
   * Creates a recurrence for the classroom.
   *
   * <ul>
   *   <li>Does not expand occurrences; {@code recurUntil} is optional.
   * </ul>
   *
   * @param classroomId classroom the recurrence belongs to
   * @param command frequency, weekday, start date, and daily times
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
   * Loads a recurrence by id scoped to the classroom.
   *
   * @param classroomId classroom the recurrence belongs to
   * @param scheduleId recurrence to load
   * @return recurrence when found in that classroom
   */
  ClassroomScheduleRecurrenceResponse getClassroomScheduleRecurrenceById(
      UUID classroomId, UUID scheduleId);

  /**
   * Soft-deletes a recurrence (sets {@code deletedDate}).
   *
   * @param classroomId classroom the recurrence belongs to
   * @param scheduleId recurrence to delete
   */
  void deleteClassroomScheduleRecurrence(UUID classroomId, UUID scheduleId);
}
