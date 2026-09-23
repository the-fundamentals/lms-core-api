package tech.sangdang.lmscoreapi.modules.management.app;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceResponse;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendancesCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomSessionAttendanceCommand;

public interface ClassroomSessionService {

  /**
   * Creates a session in the classroom.
   *
   * @param classroomId classroom the session belongs to
   * @param command date, start/end times, required status/type, optional name/description
   * @return created session
   */
  ClassroomSessionResponse createClassroomSession(
      UUID classroomId, CreateClassroomSessionCommand command);

  /**
   * Returns a session by id within the classroom.
   *
   * @param classroomId classroom the session belongs to
   * @param sessionId session to load
   * @return session
   */
  ClassroomSessionResponse getClassroomSessionById(UUID classroomId, UUID sessionId);

  /**
   * Queries sessions in the classroom.
   *
   * <ul>
   *   <li>Always scoped to {@code classroomId} in addition to the filter.
   * </ul>
   *
   * @param classroomId classroom whose sessions to query
   * @param filter paging, sort, and field filters
   * @return matching sessions
   */
  List<ClassroomSessionResponse> queryClassroomSessions(
      UUID classroomId, ClassroomSessionFilter filter);

  /**
   * Queries attendance history for a classroom member.
   *
   * <ul>
   *   <li>Removed members are still allowed (history).
   * </ul>
   *
   * @param classroomId classroom the member belongs to
   * @param memberId member whose attendance to load
   * @param filter paging, sort, and field filters
   * @return matching attendance rows
   */
  List<ClassroomSessionAttendanceResponse> queryClassroomSessionAttendancesByMember(
      UUID classroomId, UUID memberId, ClassroomSessionAttendanceFilter filter);

  /**
   * Creates attendance rows for a session.
   *
   * <ul>
   *   <li>Duplicate member ids in the request are 400; existing rows are 409.
   *   <li>Only ACTIVE members; omitted {@code attendanceDate} defaults to now.
   * </ul>
   *
   * @param classroomId classroom the session belongs to
   * @param sessionId session to record
   * @param command attendance items
   * @return created rows in request order
   */
  List<ClassroomSessionAttendanceResponse> createClassroomSessionAttendances(
      UUID classroomId, UUID sessionId, CreateClassroomSessionAttendancesCommand command);

  /**
   * Lists all attendance rows for a session.
   *
   * @param classroomId classroom the session belongs to
   * @param sessionId session whose attendance to load
   * @return attendance rows
   */
  List<ClassroomSessionAttendanceResponse> getClassroomSessionAttendances(
      UUID classroomId, UUID sessionId);

  /**
   * Updates an attendance row's status only.
   *
   * @param classroomId classroom the session belongs to
   * @param sessionId session the row belongs to
   * @param attendanceId row to update
   * @param command new status
   * @return updated row
   */
  ClassroomSessionAttendanceResponse updateClassroomSessionAttendance(
      UUID classroomId,
      UUID sessionId,
      UUID attendanceId,
      UpdateClassroomSessionAttendanceCommand command);

  /**
   * Hard-deletes an attendance row.
   *
   * @param classroomId classroom the session belongs to
   * @param sessionId session the row belongs to
   * @param attendanceId row to delete
   */
  void deleteClassroomSessionAttendance(UUID classroomId, UUID sessionId, UUID attendanceId);

  /**
   * Inserts generated sessions for active classrooms whose recurrences overlap the window.
   *
   * <ul>
   *   <li>{@code startDate} and {@code endDate} are inclusive.
   *   <li>Existing generated rows for the same classroom, recurrence, and date are skipped.
   * </ul>
   *
   * @param startDate inclusive window start
   * @param endDate inclusive window end
   */
  void generateSessionsFromRecurrence(LocalDate startDate, LocalDate endDate);
}
