package tech.sangdang.lmscoreapi.modules.management.support;

import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CREATED_AT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.MODIFIED_AT;

import java.time.LocalDateTime;
import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSchedule;

public final class ClassroomScheduleFixtures {

  public static final UUID SCHEDULE_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
  public static final String SCHEDULE_RULE = "FREQ=MONTHLY;BYMONTHDAY=10,15";
  public static final String INVALID_SCHEDULE_RULE = "not-a-recurrence-rule";
  public static final LocalDateTime DELETED_AT = LocalDateTime.of(2026, 7, 20, 10, 0, 0);

  private ClassroomScheduleFixtures() {}

  public static ClassroomSchedule classroomSchedule() {
    return classroomSchedule(SCHEDULE_ID, CLASSROOM_ID, SCHEDULE_RULE);
  }

  public static ClassroomSchedule classroomSchedule(
      UUID id, UUID classroomId, String scheduleRule) {
    return new ClassroomSchedule()
        .setId(id)
        .setClassroomId(classroomId)
        .setScheduleRule(scheduleRule)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }
}
