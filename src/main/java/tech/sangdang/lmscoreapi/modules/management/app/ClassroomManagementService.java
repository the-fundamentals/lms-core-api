package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomCommand;

public interface ClassroomManagementService {

  /**
   * Creates a classroom.
   *
   * <ul>
   *   <li>Confirms {@code bannerKey} in public storage when present.
   * </ul>
   *
   * @param command name; optional banner key
   * @return created classroom
   */
  ClassroomResponse createClassroom(CreateClassroomCommand command);

  /**
   * Updates a classroom's name and optional banner.
   *
   * <ul>
   *   <li>Omitting {@code bannerKey} leaves the existing banner.
   *   <li>Confirms a new {@code bannerKey} in public storage when present.
   * </ul>
   *
   * @param id classroom to update
   * @param command name; optional banner key
   * @return updated classroom
   */
  ClassroomResponse updateClassroom(UUID id, UpdateClassroomCommand command);

  /**
   * Returns a classroom by id.
   *
   * @param id classroom to load
   * @return classroom
   */
  ClassroomResponse getClassroomById(UUID id);

  /**
   * Queries classrooms with the given filter.
   *
   * @param filter paging, sort, and field filters
   * @return matching classrooms
   */
  List<ClassroomResponse> queryClassrooms(ClassroomFilter filter);
}
