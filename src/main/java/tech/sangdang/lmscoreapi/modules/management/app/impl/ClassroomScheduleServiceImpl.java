package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.validate.ValidationException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.common.utility.RRuleValidation;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomScheduleService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomScheduleMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSchedule;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomScheduleRepository;

@Service
@RequiredArgsConstructor
public class ClassroomScheduleServiceImpl implements ClassroomScheduleService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomScheduleRepository classroomScheduleRepository;
  private final ClassroomScheduleMapper classroomScheduleMapper;

  @Override
  @Transactional
  public ClassroomScheduleResponse createClassroomSchedule(
      UUID classroomId, CreateClassroomScheduleCommand command) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    if(!RRuleValidation.validateRecurrenceRule(command.getScheduleRule())) {
      throw new GenericBadRequestException("INVALID_RRULE", "Invalid Recurrence Rule");
    }

    ClassroomSchedule schedule =
        new ClassroomSchedule()
            .setClassroomId(classroomId)
            .setScheduleRule(command.getScheduleRule());
    return classroomScheduleMapper.toResponse(classroomScheduleRepository.insert(schedule));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomScheduleResponse> getAllClassroomSchedules(UUID classroomId) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    return classroomScheduleRepository.findByClassroomIdAndDeletedDateIsNull(classroomId).stream()
        .map(classroomScheduleMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void deleteClassroomSchedule(UUID classroomId, UUID scheduleId) {
    ClassroomSchedule schedule =
        classroomScheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomSchedule.class, scheduleId));

    if (!classroomId.equals(schedule.getClassroomId())) {
      throw ObjectNotFoundException.of(ClassroomSchedule.class, scheduleId);
    }

    schedule.setDeletedDate(LocalDateTime.now());
    classroomScheduleRepository.update(schedule);
  }
}
