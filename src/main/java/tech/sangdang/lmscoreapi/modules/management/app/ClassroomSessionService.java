package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.*;

public interface ClassroomSessionService {

  /**
   * Creates an adhoc session in the classroom.
   *
   * <ul>
   *   <li>Status is always OPEN; type is always ADHOC.
   * </ul>
   *
   * @param classroomId classroom the session belongs to
   * @param command date, start/end times, optional name/description
   * @return created session
   */
  ClassroomSessionResponse createClassroomSessionAdhoc(
      UUID classroomId, CreateClassroomSessionAdhocCommand command);

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
   * Marks an OPEN session COMPLETED.
   *
   * <ul>
   *   <li>COMPLETED and CANCELLED are 400.
   * </ul>
   *
   * @param classroomId classroom the session belongs to
   * @param sessionId session to complete
   * @return updated session
   */
  ClassroomSessionResponse completeClassroomSession(UUID classroomId, UUID sessionId);

  /**
   * Marks an OPEN session CANCELLED.
   *
   * <ul>
   *   <li>COMPLETED and CANCELLED are 400.
   * </ul>
   *
   * @param classroomId classroom the session belongs to
   * @param sessionId session to cancel
   * @return updated session
   */
  ClassroomSessionResponse cancelClassroomSession(UUID classroomId, UUID sessionId);

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
   * Lists all attendance rows for a session.
   *
   * @param classroomId classroom the session belongs to
   * @param sessionId session whose attendance to load
   * @return attendance rows
   */
  List<ClassroomSessionAttendanceResponse> getClassroomSessionAttendances(
      UUID classroomId, UUID sessionId);

  /**
   * Creates attendance rows for a session.
   *
   * <ul>
   *   <li>Duplicate member ids in the request are 400; existing rows are 409.
   *   <li>Only ACTIVE members; omitted {@code attendanceDate} defaults to now.
   *   <li>Session must be OPEN; COMPLETED and CANCELLED are 400.
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
   * Updates an attendance row's status only.
   *
   * <ul>
   *   <li>Session must be OPEN; COMPLETED and CANCELLED are 400.
   * </ul>
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
   * <ul>
   *   <li>Session must be OPEN; COMPLETED and CANCELLED are 400.
   * </ul>
   *
   * @param classroomId classroom the session belongs to
   * @param sessionId session the row belongs to
   * @param attendanceId row to delete
   */
  void deleteClassroomSessionAttendance(UUID classroomId, UUID sessionId, UUID attendanceId);
}
