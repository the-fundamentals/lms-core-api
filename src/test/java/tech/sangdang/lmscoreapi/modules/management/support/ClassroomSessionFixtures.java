package tech.sangdang.lmscoreapi.modules.management.support;

import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CREATED_AT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.MODIFIED_AT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendance;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendanceStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionType;

public final class ClassroomSessionFixtures {

  public static final UUID SESSION_ID = UUID.fromString("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d");
  public static final UUID ATTENDANCE_ID = UUID.fromString("1b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed");
  public static final UUID SECOND_ATTENDANCE_ID =
      UUID.fromString("2b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed");
  public static final LocalDate SESSION_DATE = LocalDate.of(2026, 7, 19);
  public static final String SESSION_DATE_VALUE = "2026-07-19";
  public static final LocalTime START_TIME = LocalTime.of(9, 0, 0);
  public static final LocalTime END_TIME = LocalTime.of(10, 30, 0);
  public static final String START_TIME_VALUE = "09:00:00";
  public static final String END_TIME_VALUE = "10:30:00";
  public static final LocalDateTime ATTENDANCE_DATE = LocalDateTime.of(2026, 7, 19, 9, 5, 0);
  public static final String SESSION_NAME = "Week 1 lecture";
  public static final String SESSION_DESCRIPTION =
      "Introduction to the course and classroom expectations";

  private ClassroomSessionFixtures() {}

  public static ClassroomSession classroomSession() {
    return classroomSession(SESSION_ID, CLASSROOM_ID, SESSION_DATE);
  }

  public static ClassroomSession classroomSession(
      UUID id, UUID classroomId, LocalDate sessionDate) {
    return new ClassroomSession()
        .setId(id)
        .setClassroomId(classroomId)
        .setSessionDate(sessionDate)
        .setStartTime(START_TIME)
        .setEndTime(END_TIME)
        .setName(SESSION_NAME)
        .setDescription(SESSION_DESCRIPTION)
        .setStatus(ClassroomSessionStatus.OPEN)
        .setType(ClassroomSessionType.SCHEDULE)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }

  public static ClassroomSessionAttendance classroomSessionAttendance() {
    return classroomSessionAttendance(
        ATTENDANCE_ID, SESSION_ID, MEMBER_ID, ClassroomSessionAttendanceStatus.ATTENDED);
  }

  public static ClassroomSessionAttendance classroomSessionAttendance(
      UUID id, UUID sessionId, UUID classroomMemberId, ClassroomSessionAttendanceStatus status) {
    return new ClassroomSessionAttendance()
        .setId(id)
        .setSessionId(sessionId)
        .setClassroomMemberId(classroomMemberId)
        .setAttendanceDate(ATTENDANCE_DATE)
        .setStatus(status)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }
}
