package tech.sangdang.lmscoreapi.modules.management.api;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tech.sangdang.lmscoreapi.generated.api.ClassroomFinancesApi;
import tech.sangdang.lmscoreapi.generated.model.GetClassroomRevenueQuery;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomRevenueService;

@RestController
@RequiredArgsConstructor
public class ClassroomFinancesController implements ClassroomFinancesApi {

  private final ClassroomRevenueService classroomRevenueService;

  @Override
  public ResponseEntity<?> getClassroomRevenue(
      @NonNull UUID classroomId, @NonNull GetClassroomRevenueQuery getClassroomRevenueQuery) {
    return ResponseEntity.ok(
        classroomRevenueService.getClassroomRevenue(classroomId, getClassroomRevenueQuery));
  }
}
