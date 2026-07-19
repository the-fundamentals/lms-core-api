package tech.sangdang.lmscoreapi.modules.classroom.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceResponse;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendanceCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionCommand;

public interface ClassroomSessionService {

  ClassroomSessionResponse createClassroomSession(
      UUID classroomId, CreateClassroomSessionCommand command);

  ClassroomSessionResponse getClassroomSessionById(UUID classroomId, UUID sessionId);

  List<ClassroomSessionResponse> queryClassroomSessions(
      UUID classroomId, ClassroomSessionFilter filter);

  void deleteClassroomSession(UUID classroomId, UUID sessionId);

  ClassroomSessionAttendanceResponse createClassroomSessionAttendance(
      UUID classroomId, UUID sessionId, CreateClassroomSessionAttendanceCommand command);

  void deleteClassroomSessionAttendance(UUID classroomId, UUID sessionId, UUID attendanceId);
}
