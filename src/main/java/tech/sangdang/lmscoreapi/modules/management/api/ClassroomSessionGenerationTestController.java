package tech.sangdang.lmscoreapi.modules.management.api;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.sangdang.lmscoreapi.common.TestController;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomSessionService;

// Not in OpenAPI: local/test-only throwaway, unlike /test/storage which is on TestApi
@TestController
@RequiredArgsConstructor
@RequestMapping("/test/classroom-sessions")
public class ClassroomSessionGenerationTestController {

  private final ClassroomSessionService classroomSessionService;

  @PostMapping("/generate-from-recurrence")
  public ResponseEntity<Void> generateFromRecurrence(
      @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
    classroomSessionService.generateSessionsFromRecurrence(startDate, endDate);
    return ResponseEntity.noContent().build();
  }
}
