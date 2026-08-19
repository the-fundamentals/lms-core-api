package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceResponse;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendancesCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionCommand;

public interface ClassroomSessionService {

  ClassroomSessionResponse createClassroomSession(
      UUID classroomId, CreateClassroomSessionCommand command);

  ClassroomSessionResponse getClassroomSessionById(UUID classroomId, UUID sessionId);

  List<ClassroomSessionResponse> queryClassroomSessions(
      UUID classroomId, ClassroomSessionFilter filter);

  List<ClassroomSessionAttendanceResponse> queryClassroomSessionAttendancesByMember(
      UUID classroomId, UUID memberId, ClassroomSessionAttendanceFilter filter);

  void deleteClassroomSession(UUID classroomId, UUID sessionId);

  List<ClassroomSessionAttendanceResponse> createClassroomSessionAttendances(
      UUID classroomId, UUID sessionId, CreateClassroomSessionAttendancesCommand command);

  List<ClassroomSessionAttendanceResponse> getClassroomSessionAttendances(
      UUID classroomId, UUID sessionId);

  void deleteClassroomSessionAttendance(UUID classroomId, UUID sessionId, UUID attendanceId);
}
