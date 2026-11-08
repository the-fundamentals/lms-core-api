package tech.sangdang.lmscoreapi.modules.management.app;

import java.util.List;
import java.util.UUID;
import tech.sangdang.lmscoreapi.generated.model.ClassroomFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomCommand;
import tech.sangdang.lmscoreapi.generated.model.SetClassroomBasePaymentPlanCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomCommand;

public interface ClassroomManagementService {

  /**
   * Creates a classroom.
   *
   * <ul>
   *   <li>Confirms {@code bannerKey} in public storage when present.
   *   <li>Status is always ACTIVE; not taken from the command.
   *   <li>Omitted base payment plan is PER_SESSION, amount 0.
   * </ul>
   *
   * @param command name; optional banner key and base payment plan
   * @return created classroom
   */
  ClassroomResponse createClassroom(CreateClassroomCommand command);

  /**
   * Sets the classroom base payment plan.
   *
   * <ul>
   *   <li>Omitted type is PER_SESSION.
   *   <li>When type or amount changes, replaces the current plan of each ACTIVE STUDENT still
   *       copied from the classroom base.
   *   <li>An unchanged base does not rewrite member plans.
   * </ul>
   *
   * @param classroomId classroom whose base to set
   * @param command amount; optional type
   * @return updated classroom
   */
  ClassroomResponse setClassroomBasePaymentPlan(
      UUID classroomId, SetClassroomBasePaymentPlanCommand command);

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
