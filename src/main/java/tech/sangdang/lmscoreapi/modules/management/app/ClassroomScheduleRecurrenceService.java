package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleRecurrenceResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleRecurrenceCommand;

public interface ClassroomScheduleRecurrenceService {

  ClassroomScheduleRecurrenceResponse createClassroomScheduleRecurrence(
      UUID classroomId, CreateClassroomScheduleRecurrenceCommand command);

  List<ClassroomScheduleRecurrenceResponse> getAllClassroomScheduleRecurrences(UUID classroomId);

  void deleteClassroomScheduleRecurrence(UUID classroomId, UUID scheduleId);
}
