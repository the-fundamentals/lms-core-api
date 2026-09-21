package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.Utilities;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleRecurrenceResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleRecurrenceCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomScheduleRecurrenceService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomScheduleRecurrenceMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleRecurrence;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomScheduleRecurrenceRepository;

@Service
@RequiredArgsConstructor
public class ClassroomScheduleRecurrenceServiceImpl implements ClassroomScheduleRecurrenceService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomScheduleRecurrenceRepository classroomScheduleRecurrenceRepository;
  private final ClassroomScheduleRecurrenceMapper classroomScheduleRecurrenceMapper;

  @Override
  @Transactional
  public ClassroomScheduleRecurrenceResponse createClassroomScheduleRecurrence(
      UUID classroomId, CreateClassroomScheduleRecurrenceCommand command) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    ClassroomScheduleRecurrence recurrence =
        new ClassroomScheduleRecurrence()
            .setClassroomId(classroomId)
            .assignScheduleRule(command.getScheduleRule())
            .setRecurrenceStartDate(command.getRecurrenceStartDate())
            .setStartTime(Utilities.parseTimeOrError(command.getStartTime()))
            .setEndTime(Utilities.parseTimeOrError(command.getEndTime()));
    return classroomScheduleRecurrenceMapper.toResponse(
        classroomScheduleRecurrenceRepository.insert(recurrence));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomScheduleRecurrenceResponse> getAllClassroomScheduleRecurrences(
      UUID classroomId) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    return classroomScheduleRecurrenceRepository
        .findByClassroomIdAndDeletedDateIsNull(classroomId)
        .stream()
        .map(classroomScheduleRecurrenceMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void deleteClassroomScheduleRecurrence(UUID classroomId, UUID scheduleId) {
    ClassroomScheduleRecurrence recurrence =
        classroomScheduleRecurrenceRepository
            .findById(scheduleId)
            .orElseThrow(
                () -> ObjectNotFoundException.of(ClassroomScheduleRecurrence.class, scheduleId));

    if (!classroomId.equals(recurrence.getClassroomId())) {
      throw ObjectNotFoundException.of(ClassroomScheduleRecurrence.class, scheduleId);
    }

    recurrence.setDeletedDate(LocalDateTime.now());
    classroomScheduleRecurrenceRepository.update(recurrence);
  }
}
