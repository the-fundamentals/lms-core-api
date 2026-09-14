package tech.sangdang.lmscoreapi.modules.management.app;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleCancelledResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleCancelledCommand;

public interface ClassroomScheduleCancelledService {

  /**
   * Records a cancelled occurrence of a recurring classroom schedule.
   *
   * @param classroomId classroom the occurrence belongs to
   * @param command date and start/end times of the cancelled window
   */
  ClassroomScheduleCancelledResponse createClassroomScheduleCancelled(
      UUID classroomId, CreateClassroomScheduleCancelledCommand command);

  /**
   * Returns cancelled occurrences for the classroom, optionally filtered by inclusive date range.
   *
   * @param classroomId classroom whose cancelled occurrences to load
   * @param startDate inclusive start of `date` filter; null to leave open
   * @param endDate inclusive end of `date` filter; null to leave open
   */
  List<ClassroomScheduleCancelledResponse> getAllClassroomScheduleCancelleds(
      UUID classroomId, LocalDate startDate, LocalDate endDate);

  /**
   * Permanently deletes a cancelled-occurrence record.
   *
   * @param classroomId classroom the occurrence belongs to
   * @param cancelledId record to delete
   */
  void deleteClassroomScheduleCancelled(UUID classroomId, UUID cancelledId);
}
