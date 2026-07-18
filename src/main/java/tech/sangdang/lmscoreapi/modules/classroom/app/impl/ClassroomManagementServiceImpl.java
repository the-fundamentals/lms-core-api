package tech.sangdang.lmscoreapi.modules.classroom.app.impl;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.ClassroomFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomCommand;
import tech.sangdang.lmscoreapi.modules.classroom.app.ClassroomManagementService;
import tech.sangdang.lmscoreapi.modules.classroom.app.mappers.ClassroomMapper;
import tech.sangdang.lmscoreapi.modules.classroom.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.classroom.dom.repository.ClassroomRepository;

@Service
@RequiredArgsConstructor
public class ClassroomManagementServiceImpl implements ClassroomManagementService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomMapper classroomMapper;

  @Override
  @Transactional
  public ClassroomResponse createClassroom(CreateClassroomCommand command) {
    Classroom classroom = new Classroom().setName(command.getName());
    return classroomMapper.toResponse(classroomRepository.insert(classroom));
  }

  @Override
  @Transactional
  public ClassroomResponse updateClassroom(UUID id, UpdateClassroomCommand command) {
    Classroom classroom =
        classroomRepository
            .findById(id)
            .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, id));
    classroom.setName(command.getName());
    return classroomMapper.toResponse(classroomRepository.update(classroom));
  }

  @Override
  @Transactional(readOnly = true)
  public ClassroomResponse getClassroomById(UUID id) {
    return classroomMapper.toResponse(
        classroomRepository
            .findById(id)
            .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, id)));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomResponse> queryClassrooms(ClassroomFilter filter) {
    return classroomRepository
        .query(classroomMapper.toBaseQuery(filter))
        .map(classroomMapper::toResponse)
        .toList();
  }
}
