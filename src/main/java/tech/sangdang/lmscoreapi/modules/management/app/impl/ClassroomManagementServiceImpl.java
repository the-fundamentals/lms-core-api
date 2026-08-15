package tech.sangdang.lmscoreapi.modules.management.app.impl;

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
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomManagementService;
import tech.sangdang.lmscoreapi.modules.management.app.internal.ClassroomRecordService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.utility.app.StorageService;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;

@Service
@RequiredArgsConstructor
public class ClassroomManagementServiceImpl implements ClassroomManagementService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomMapper classroomMapper;
  private final ClassroomRecordService classroomRecordService;
  private final StorageService storageService;

  @Override
  public ClassroomResponse createClassroom(CreateClassroomCommand command) {
    Classroom classroom = classroomRecordService.createClassroom(command);
    if (command.getBannerKey() != null) {
      storageService.confirmPublicFileUpload(
          new ConfirmUploadPublicCommand(command.getBannerKey()));
    }
    return classroomMapper.toResponse(classroom);
  }

  @Override
  public ClassroomResponse updateClassroom(UUID id, UpdateClassroomCommand command) {
    Classroom classroom = classroomRecordService.updateClassroom(id, command);
    if (command.getBannerKey() != null) {
      storageService.confirmPublicFileUpload(
          new ConfirmUploadPublicCommand(command.getBannerKey()));
    }
    return classroomMapper.toResponse(classroom);
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
