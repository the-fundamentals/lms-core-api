package tech.sangdang.lmscoreapi.modules.management.infra.jobs;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomSessionGenerationService;

@RequiredArgsConstructor
@Component
public class ClassroomSessionGenerationJob {
  private final ClassroomSessionGenerationService classroomSessionGenerationService;

  // 7th of each month 00:00 — generates the following calendar month of classroom sessions
  @Scheduled(cron = "${app.modules.management.session-generation-cron:0 0 0 7 * *}")
  public void generateClassroomSessionsFromScheduleRecurrence() {
    classroomSessionGenerationService.generateSessionsFromRecurrence(LocalDate.now());
  }
}
