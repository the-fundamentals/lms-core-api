package tech.sangdang.lmscoreapi.modules.management.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomScheduleRecurrenceApi;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleRecurrenceCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomScheduleRecurrenceService;

@RestController
@RequiredArgsConstructor
public class ClassroomScheduleRecurrenceController implements ClassroomScheduleRecurrenceApi {

  private final ClassroomScheduleRecurrenceService classroomScheduleRecurrenceService;

  @Override
  public ResponseEntity<?> createClassroomScheduleRecurrence(
      @NonNull UUID classroomId,
      @NonNull CreateClassroomScheduleRecurrenceCommand createClassroomScheduleRecurrenceCommand) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            classroomScheduleRecurrenceService.createClassroomScheduleRecurrence(
                classroomId, createClassroomScheduleRecurrenceCommand));
  }

  @Override
  public ResponseEntity<?> getAllClassroomScheduleRecurrences(@NonNull UUID classroomId) {
    return ResponseEntity.ok(
        classroomScheduleRecurrenceService.getAllClassroomScheduleRecurrences(classroomId));
  }

  @Override
  public ResponseEntity<?> deleteClassroomScheduleRecurrence(
      @NonNull UUID classroomId, @NonNull UUID scheduleId) {
    classroomScheduleRecurrenceService.deleteClassroomScheduleRecurrence(classroomId, scheduleId);
    return ResponseEntity.noContent().build();
  }
}
