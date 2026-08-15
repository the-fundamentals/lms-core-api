package tech.sangdang.lmscoreapi.modules.management.app.internal;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.InternalService;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomCommand;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;

@RequiredArgsConstructor
@InternalService
public class ClassroomRecordService {
  private final ClassroomRepository classroomRepository;

  @Transactional
  public Classroom createClassroom(@NonNull CreateClassroomCommand command) {
    Classroom classroom =
        new Classroom()
            .setName(command.getName())
            .setBannerKey(command.getBannerKey())
            .setNumberOfMembers(0);
    return classroomRepository.insert(classroom);
  }

  @Transactional
  public Classroom updateClassroom(@NonNull UUID id, @NonNull UpdateClassroomCommand command) {
    Classroom classroom =
        classroomRepository
            .findById(id)
            .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, id));
    classroom.setName(command.getName());
    if (command.getBannerKey() != null) {
      classroom.setBannerKey(command.getBannerKey());
    }
    return classroomRepository.update(classroom);
  }

  @Transactional
  public void adjustNumberOfMembers(@NonNull UUID classroomId, int delta) {
    Classroom classroom =
        classroomRepository
            .findById(classroomId)
            .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));
    int current = classroom.getNumberOfMembers() == null ? 0 : classroom.getNumberOfMembers();
    classroom.setNumberOfMembers(Math.max(0, current + delta));
    classroomRepository.update(classroom);
  }
}
