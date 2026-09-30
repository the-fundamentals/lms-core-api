package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleRecurrenceResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleRecurrenceCommand;
import tech.sangdang.lmscoreapi.generated.model.RecurrenceByDay;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomScheduleRecurrenceService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomScheduleRecurrenceMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleRecurrence;
import tech.sangdang.lmscoreapi.modules.management.dom.RecurrenceFrequency;
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
  public List<ClassroomScheduleRecurrenceResponse> createClassroomScheduleRecurrence(
      UUID classroomId, CreateClassroomScheduleRecurrenceCommand command) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    RecurrenceFrequency frequency = RecurrenceFrequency.valueOf(command.getFrequency().getValue());
    List<ClassroomScheduleRecurrence> toInsert = new ArrayList<>(command.getByDays().size());
    for (RecurrenceByDay byDay : command.getByDays()) {
      toInsert.add(
          new ClassroomScheduleRecurrence()
              .setClassroomId(classroomId)
              .setFrequency(frequency)
              .setByDay(DayOfWeek.valueOf(byDay.getValue()))
              .setRecurrenceStartDate(command.getRecurrenceStartDate())
              .setRecurUntil(command.getRecurUntil())
              .setStartTime(command.getStartTime())
              .setEndTime(command.getEndTime())
              .setName(command.getName())
              .setDescription(command.getDescription()));
    }

    return classroomScheduleRecurrenceRepository.insertAll(toInsert).stream()
        .map(classroomScheduleRecurrenceMapper::toResponse)
        .toList();
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
  @Transactional(readOnly = true)
  public ClassroomScheduleRecurrenceResponse getClassroomScheduleRecurrenceById(
      UUID classroomId, UUID scheduleId) {
    return classroomScheduleRecurrenceMapper.toResponse(
        requireRecurrenceInClassroom(classroomId, scheduleId));
  }

  @Override
  @Transactional
  public void deleteClassroomScheduleRecurrence(UUID classroomId, UUID scheduleId) {
    ClassroomScheduleRecurrence recurrence = requireRecurrenceInClassroom(classroomId, scheduleId);
    recurrence.setDeletedDate(LocalDateTime.now());
    classroomScheduleRecurrenceRepository.update(recurrence);
  }

  // Same classroom scoping as requireSessionInClassroom: missing or wrong classroom → 404
  private ClassroomScheduleRecurrence requireRecurrenceInClassroom(
      UUID classroomId, UUID scheduleId) {
    ClassroomScheduleRecurrence recurrence =
        classroomScheduleRecurrenceRepository
            .findById(scheduleId)
            .orElseThrow(
                () -> ObjectNotFoundException.of(ClassroomScheduleRecurrence.class, scheduleId));

    if (!classroomId.equals(recurrence.getClassroomId())) {
      throw ObjectNotFoundException.of(ClassroomScheduleRecurrence.class, scheduleId);
    }
    return recurrence;
  }
}
