package tech.sangdang.lmscoreapi.modules.management.infra.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomSessionService;

@RequiredArgsConstructor
@Component
public class ClassroomSessionGenerationJob {
  private final ClassroomSessionService classroomSessionService;

//  @Scheduled()
//  public void generateClassroomSessionsFromScheduleRecurrence() {}
}
