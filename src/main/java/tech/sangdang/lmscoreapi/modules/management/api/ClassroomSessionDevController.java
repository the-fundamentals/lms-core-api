package tech.sangdang.lmscoreapi.modules.management.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import tech.sangdang.lmscoreapi.common.DevController;
import tech.sangdang.lmscoreapi.generated.api.DevApi;
import tech.sangdang.lmscoreapi.generated.model.GenerateClassroomSessionsFromRecurrenceCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomSessionGenerationService;

@DevController
@RequiredArgsConstructor
public class ClassroomSessionDevController implements DevApi {

  private final ClassroomSessionGenerationService classroomSessionGenerationService;

  @Override
  public ResponseEntity<?> generateClassroomSessionsFromRecurrence(
      GenerateClassroomSessionsFromRecurrenceCommand command) {
    classroomSessionGenerationService.generateSessionsFromRecurrence(command.getRunDate());
    return ResponseEntity.noContent().build();
  }
}
