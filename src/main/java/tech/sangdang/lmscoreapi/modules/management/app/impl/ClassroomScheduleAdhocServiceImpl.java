package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.Utilities;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleAdhocResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleAdhocCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomScheduleAdhocService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomScheduleAdhocMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleAdhoc;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomScheduleAdhocRepository;

@Service
@RequiredArgsConstructor
public class ClassroomScheduleAdhocServiceImpl implements ClassroomScheduleAdhocService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomScheduleAdhocRepository classroomScheduleAdhocRepository;
  private final ClassroomScheduleAdhocMapper classroomScheduleAdhocMapper;

  @Override
  @Transactional
  public ClassroomScheduleAdhocResponse createClassroomScheduleAdhoc(
      UUID classroomId, CreateClassroomScheduleAdhocCommand command) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    ClassroomScheduleAdhoc adhoc =
        new ClassroomScheduleAdhoc()
            .setClassroomId(classroomId)
            .setDate(command.getDate())
            .setStartTime(Utilities.parseTimeOrError(command.getStartTime()))
            .setEndTime(Utilities.parseTimeOrError(command.getEndTime()));
    return classroomScheduleAdhocMapper.toResponse(classroomScheduleAdhocRepository.insert(adhoc));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomScheduleAdhocResponse> getAllClassroomScheduleAdhocs(
      UUID classroomId, LocalDate startDate, LocalDate endDate) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));
    Utilities.requireInclusiveDateRange(startDate, endDate);

    return classroomScheduleAdhocRepository
        .findByClassroomId(classroomId, startDate, endDate)
        .stream()
        .map(classroomScheduleAdhocMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void deleteClassroomScheduleAdhoc(UUID classroomId, UUID adhocId) {
    ClassroomScheduleAdhoc adhoc =
        classroomScheduleAdhocRepository
            .findById(adhocId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomScheduleAdhoc.class, adhocId));

    if (!classroomId.equals(adhoc.getClassroomId())) {
      throw ObjectNotFoundException.of(ClassroomScheduleAdhoc.class, adhocId);
    }

    classroomScheduleAdhocRepository.deleteById(adhocId);
  }
}
