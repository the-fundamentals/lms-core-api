package tech.sangdang.lmscoreapi.modules.management.support;

import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CREATED_AT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.MODIFIED_AT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.END_TIME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleFixtures.START_TIME;

import java.time.LocalDate;
import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleAdhoc;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleCancelled;

public final class ClassroomScheduleOccurrenceFixtures {

  public static final UUID ADHOC_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
  public static final UUID CANCELLED_ID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
  public static final LocalDate OCCURRENCE_DATE = LocalDate.of(2026, 7, 20);
  public static final String OCCURRENCE_DATE_VALUE = "2026-07-20";

  private ClassroomScheduleOccurrenceFixtures() {}

  public static ClassroomScheduleAdhoc classroomScheduleAdhoc() {
    return classroomScheduleAdhoc(ADHOC_ID, CLASSROOM_ID);
  }

  public static ClassroomScheduleAdhoc classroomScheduleAdhoc(UUID id, UUID classroomId) {
    return new ClassroomScheduleAdhoc()
        .setId(id)
        .setClassroomId(classroomId)
        .setDate(OCCURRENCE_DATE)
        .setStartTime(START_TIME)
        .setEndTime(END_TIME)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }

  public static ClassroomScheduleCancelled classroomScheduleCancelled() {
    return classroomScheduleCancelled(CANCELLED_ID, CLASSROOM_ID);
  }

  public static ClassroomScheduleCancelled classroomScheduleCancelled(UUID id, UUID classroomId) {
    return new ClassroomScheduleCancelled()
        .setId(id)
        .setClassroomId(classroomId)
        .setDate(OCCURRENCE_DATE)
        .setStartTime(START_TIME)
        .setEndTime(END_TIME)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }
}
