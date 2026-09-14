package tech.sangdang.lmscoreapi.modules.management.api;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomScheduleAdhocApi;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleAdhocCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomScheduleAdhocService;

@RestController
@RequiredArgsConstructor
public class ClassroomScheduleAdhocController implements ClassroomScheduleAdhocApi {

  private final ClassroomScheduleAdhocService classroomScheduleAdhocService;

  @Override
  public ResponseEntity<?> createClassroomScheduleAdhoc(
      @NonNull UUID classroomId,
      @NonNull CreateClassroomScheduleAdhocCommand createClassroomScheduleAdhocCommand) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            classroomScheduleAdhocService.createClassroomScheduleAdhoc(
                classroomId, createClassroomScheduleAdhocCommand));
  }

  @Override
  public ResponseEntity<?> getAllClassroomScheduleAdhocs(
      @NonNull UUID classroomId, @Nullable LocalDate startDate, @Nullable LocalDate endDate) {
    return ResponseEntity.ok(
        classroomScheduleAdhocService.getAllClassroomScheduleAdhocs(
            classroomId, startDate, endDate));
  }

  @Override
  public ResponseEntity<?> deleteClassroomScheduleAdhoc(
      @NonNull UUID classroomId, @NonNull UUID adhocId) {
    classroomScheduleAdhocService.deleteClassroomScheduleAdhoc(classroomId, adhocId);
    return ResponseEntity.noContent().build();
  }
}
