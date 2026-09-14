package tech.sangdang.lmscoreapi.modules.management.app;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleAdhocResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleAdhocCommand;

public interface ClassroomScheduleAdhocService {

  /**
   * Creates an ad-hoc (one-off) schedule occurrence for the classroom.
   *
   * @param classroomId classroom the occurrence belongs to
   * @param command date and start/end times
   */
  ClassroomScheduleAdhocResponse createClassroomScheduleAdhoc(
      UUID classroomId, CreateClassroomScheduleAdhocCommand command);

  /**
   * Returns ad-hoc occurrences for the classroom, optionally filtered by inclusive date range.
   *
   * @param classroomId classroom whose ad-hoc occurrences to load
   * @param startDate inclusive start of `date` filter; null to leave open
   * @param endDate inclusive end of `date` filter; null to leave open
   */
  List<ClassroomScheduleAdhocResponse> getAllClassroomScheduleAdhocs(
      UUID classroomId, LocalDate startDate, LocalDate endDate);

  /**
   * Permanently deletes an ad-hoc occurrence.
   *
   * @param classroomId classroom the occurrence belongs to
   * @param adhocId occurrence to delete
   */
  void deleteClassroomScheduleAdhoc(UUID classroomId, UUID adhocId);
}
