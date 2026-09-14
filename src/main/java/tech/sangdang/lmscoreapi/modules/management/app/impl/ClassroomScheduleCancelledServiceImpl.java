package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.Utilities;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleCancelledResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomScheduleCancelledCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomScheduleCancelledService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomScheduleCancelledMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleCancelled;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomScheduleCancelledRepository;

@Service
@RequiredArgsConstructor
public class ClassroomScheduleCancelledServiceImpl implements ClassroomScheduleCancelledService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomScheduleCancelledRepository classroomScheduleCancelledRepository;
  private final ClassroomScheduleCancelledMapper classroomScheduleCancelledMapper;

  @Override
  @Transactional
  public ClassroomScheduleCancelledResponse createClassroomScheduleCancelled(
      UUID classroomId, CreateClassroomScheduleCancelledCommand command) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    ClassroomScheduleCancelled cancelled =
        new ClassroomScheduleCancelled()
            .setClassroomId(classroomId)
            .setDate(command.getDate())
            .setStartTime(Utilities.parseTimeOrError(command.getStartTime()))
            .setEndTime(Utilities.parseTimeOrError(command.getEndTime()));
    return classroomScheduleCancelledMapper.toResponse(
        classroomScheduleCancelledRepository.insert(cancelled));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomScheduleCancelledResponse> getAllClassroomScheduleCancelleds(
      UUID classroomId, LocalDate startDate, LocalDate endDate) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));
    Utilities.requireInclusiveDateRange(startDate, endDate);

    return classroomScheduleCancelledRepository
        .findByClassroomId(classroomId, startDate, endDate)
        .stream()
        .map(classroomScheduleCancelledMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void deleteClassroomScheduleCancelled(UUID classroomId, UUID cancelledId) {
    ClassroomScheduleCancelled cancelled =
        classroomScheduleCancelledRepository
            .findById(cancelledId)
            .orElseThrow(
                () -> ObjectNotFoundException.of(ClassroomScheduleCancelled.class, cancelledId));

    if (!classroomId.equals(cancelled.getClassroomId())) {
      throw ObjectNotFoundException.of(ClassroomScheduleCancelled.class, cancelledId);
    }

    classroomScheduleCancelledRepository.deleteById(cancelledId);
  }
}
