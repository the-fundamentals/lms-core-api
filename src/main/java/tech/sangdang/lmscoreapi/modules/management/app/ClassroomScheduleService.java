package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleCommand;

public interface ClassroomScheduleService {

  ClassroomScheduleResponse createClassroomSchedule(
      UUID classroomId, CreateClassroomScheduleCommand command);

  List<ClassroomScheduleResponse> getAllClassroomSchedules(UUID classroomId);

  void deleteClassroomSchedule(UUID classroomId, UUID scheduleId);
}
