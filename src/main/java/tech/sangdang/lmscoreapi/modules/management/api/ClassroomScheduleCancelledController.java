package tech.sangdang.lmscoreapi.modules.management.api;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomScheduleCancelledApi;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleCancelledCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomScheduleCancelledService;

@RestController
@RequiredArgsConstructor
public class ClassroomScheduleCancelledController implements ClassroomScheduleCancelledApi {

  private final ClassroomScheduleCancelledService classroomScheduleCancelledService;

  @Override
  public ResponseEntity<?> createClassroomScheduleCancelled(
      @NonNull UUID classroomId,
      @NonNull CreateClassroomScheduleCancelledCommand createClassroomScheduleCancelledCommand) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            classroomScheduleCancelledService.createClassroomScheduleCancelled(
                classroomId, createClassroomScheduleCancelledCommand));
  }

  @Override
  public ResponseEntity<?> getAllClassroomScheduleCancelleds(
      @NonNull UUID classroomId, @Nullable LocalDate startDate, @Nullable LocalDate endDate) {
    return ResponseEntity.ok(
        classroomScheduleCancelledService.getAllClassroomScheduleCancelleds(
            classroomId, startDate, endDate));
  }

  @Override
  public ResponseEntity<?> deleteClassroomScheduleCancelled(
      @NonNull UUID classroomId, @NonNull UUID cancelledId) {
    classroomScheduleCancelledService.deleteClassroomScheduleCancelled(classroomId, cancelledId);
    return ResponseEntity.noContent().build();
  }
}
