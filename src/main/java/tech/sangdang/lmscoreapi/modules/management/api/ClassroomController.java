package tech.sangdang.lmscoreapi.modules.management.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomApi;
import tech.sangdang.lmscoreapi.generated.model.ClassroomFilter;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomManagementService;

@RestController
@RequiredArgsConstructor
public class ClassroomController implements ClassroomApi {

  private final ClassroomManagementService classroomManagementService;

  @Override
  public ResponseEntity<?> createClassroom(@NonNull CreateClassroomCommand createClassroomCommand) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(classroomManagementService.createClassroom(createClassroomCommand));
  }

  @Override
  public ResponseEntity<?> getClassroomById(@NonNull UUID id) {
    return ResponseEntity.ok(classroomManagementService.getClassroomById(id));
  }

  @Override
  public ResponseEntity<?> updateClassroom(
      @NonNull UUID id, @NonNull UpdateClassroomCommand updateClassroomCommand) {
    return ResponseEntity.ok(
        classroomManagementService.updateClassroom(id, updateClassroomCommand));
  }

  @Override
  public ResponseEntity<?> getAllClassrooms(@NonNull ClassroomFilter classroomFilter) {
    return ResponseEntity.ok(classroomManagementService.queryClassrooms(classroomFilter));
  }
}
