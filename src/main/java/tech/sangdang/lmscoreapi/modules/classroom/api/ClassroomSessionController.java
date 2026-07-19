package tech.sangdang.lmscoreapi.modules.classroom.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomSessionsApi;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendanceCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionCommand;
import tech.sangdang.lmscoreapi.modules.classroom.app.ClassroomSessionService;

@RestController
@RequiredArgsConstructor
public class ClassroomSessionController implements ClassroomSessionsApi {

  private final ClassroomSessionService classroomSessionService;

  @Override
  public ResponseEntity<?> createClassroomSession(
      @NonNull UUID classroomId,
      @NonNull CreateClassroomSessionCommand createClassroomSessionCommand) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            classroomSessionService.createClassroomSession(
                classroomId, createClassroomSessionCommand));
  }

  @Override
  public ResponseEntity<?> getAllClassroomSessions(
      @NonNull UUID classroomId, @NonNull ClassroomSessionFilter classroomSessionFilter) {
    return ResponseEntity.ok(
        classroomSessionService.queryClassroomSessions(classroomId, classroomSessionFilter));
  }

  @Override
  public ResponseEntity<?> getClassroomSessionById(
      @NonNull UUID classroomId, @NonNull UUID sessionId) {
    return ResponseEntity.ok(
        classroomSessionService.getClassroomSessionById(classroomId, sessionId));
  }

  @Override
  public ResponseEntity<?> deleteClassroomSession(
      @NonNull UUID classroomId, @NonNull UUID sessionId) {
    classroomSessionService.deleteClassroomSession(classroomId, sessionId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<?> createClassroomSessionAttendance(
      @NonNull UUID classroomId,
      @NonNull UUID sessionId,
      @NonNull CreateClassroomSessionAttendanceCommand createClassroomSessionAttendanceCommand) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            classroomSessionService.createClassroomSessionAttendance(
                classroomId, sessionId, createClassroomSessionAttendanceCommand));
  }

  @Override
  public ResponseEntity<?> deleteClassroomSessionAttendance(
      @NonNull UUID classroomId, @NonNull UUID sessionId, @NonNull UUID attendanceId) {
    classroomSessionService.deleteClassroomSessionAttendance(
        classroomId, sessionId, attendanceId);
    return ResponseEntity.noContent().build();
  }
}
