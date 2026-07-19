package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomCommand;

public interface ClassroomManagementService {

  ClassroomResponse createClassroom(CreateClassroomCommand command);

  ClassroomResponse updateClassroom(UUID id, UpdateClassroomCommand command);

  ClassroomResponse getClassroomById(UUID id);

  List<ClassroomResponse> queryClassrooms(ClassroomFilter filter);
}
