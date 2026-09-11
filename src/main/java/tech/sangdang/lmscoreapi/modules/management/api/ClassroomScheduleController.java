package tech.sangdang.lmscoreapi.modules.management.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomScheduleApi;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomScheduleService;

@RestController
@RequiredArgsConstructor
public class ClassroomScheduleController implements ClassroomScheduleApi {

  private final ClassroomScheduleService classroomScheduleService;

  @Override
  public ResponseEntity<?> createClassroomSchedule(
      @NonNull UUID classroomId,
      @NonNull CreateClassroomScheduleCommand createClassroomScheduleCommand) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            classroomScheduleService.createClassroomSchedule(
                classroomId, createClassroomScheduleCommand));
  }

  @Override
  public ResponseEntity<?> getAllClassroomSchedules(@NonNull UUID classroomId) {
    return ResponseEntity.ok(classroomScheduleService.getAllClassroomSchedules(classroomId));
  }

  @Override
  public ResponseEntity<?> deleteClassroomSchedule(
      @NonNull UUID classroomId, @NonNull UUID scheduleId) {
    classroomScheduleService.deleteClassroomSchedule(classroomId, scheduleId);
    return ResponseEntity.noContent().build();
  }
}
