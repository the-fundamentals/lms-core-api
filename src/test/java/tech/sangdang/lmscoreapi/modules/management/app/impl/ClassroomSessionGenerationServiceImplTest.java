package tech.sangdang.lmscoreapi.modules.management.app.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.classroom;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.END_TIME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.SCHEDULE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.START_TIME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomScheduleRecurrenceFixtures.classroomScheduleRecurrence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleRecurrence;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionType;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomScheduleRecurrenceRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomSessionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Classroom session generation")
class ClassroomSessionGenerationServiceImplTest {

  private static final LocalDate RUN_DATE = LocalDate.of(2026, 10, 7);
  private static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
  private static final LocalDate NOV_30 = LocalDate.of(2026, 11, 30);

  @Mock private ClassroomRepository classroomRepository;
  @Mock private ClassroomSessionRepository classroomSessionRepository;
  @Mock private ClassroomScheduleRecurrenceRepository classroomScheduleRecurrenceRepository;

  private ClassroomSessionGenerationServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new ClassroomSessionGenerationServiceImpl(
            classroomRepository, classroomSessionRepository, classroomScheduleRecurrenceRepository);
  }

  @Test
  @DisplayName("queries recurrences for the calendar month after runDate")
  void generateSessionsFromRecurrence_runDate7Oct_queriesNovemberWindow() {
    when(classroomRepository.query(any(BaseQuery.class))).thenReturn(Stream.of(classroom()));
    when(classroomScheduleRecurrenceRepository.findActiveByClassroomIdsAsOfDate(
            any(UUID[].class), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());

    service.generateSessionsFromRecurrence(RUN_DATE);

    ArgumentCaptor<UUID[]> idsCaptor = ArgumentCaptor.forClass(UUID[].class);
    verify(classroomScheduleRecurrenceRepository)
        .findActiveByClassroomIdsAsOfDate(idsCaptor.capture(), eq(NOV_1), eq(NOV_30));
    assertThat(idsCaptor.getValue()).containsExactly(CLASSROOM_ID);
  }

  @Test
  @DisplayName("inserts OPEN SCHEDULE sessions for each November Monday")
  void generateSessionsFromRecurrence_mondayWeekly_insertsFiveNovemberMondays() {
    stubActiveClassroomAndRecurrence(classroomScheduleRecurrence());

    service.generateSessionsFromRecurrence(RUN_DATE);

    List<ClassroomSession> inserted = capturedInserts();
    assertThat(inserted)
        .extracting(ClassroomSession::getSessionDate)
        .containsExactly(
            LocalDate.of(2026, 11, 2),
            LocalDate.of(2026, 11, 9),
            LocalDate.of(2026, 11, 16),
            LocalDate.of(2026, 11, 23),
            LocalDate.of(2026, 11, 30));
    assertThat(inserted)
        .allSatisfy(
            session -> {
              assertThat(session.getStatus()).isEqualTo(ClassroomSessionStatus.OPEN);
              assertThat(session.getType()).isEqualTo(ClassroomSessionType.SCHEDULE);
              assertThat(session.getStartTime()).isEqualTo(START_TIME);
              assertThat(session.getEndTime()).isEqualTo(END_TIME);
              assertThat(session.getGeneratedBy()).isEqualTo(SCHEDULE_ID);
              assertThat(session.getClassroomId()).isEqualTo(CLASSROOM_ID);
            });
  }

  @Test
  @DisplayName("stops generating Mondays after recurUntil")
  void generateSessionsFromRecurrence_recurUntilMidNovember_stopsAtUntil() {
    ClassroomScheduleRecurrence recurrence =
        classroomScheduleRecurrence().setRecurUntil(LocalDate.of(2026, 11, 16));
    stubActiveClassroomAndRecurrence(recurrence);

    service.generateSessionsFromRecurrence(RUN_DATE);

    assertThat(capturedInserts())
        .extracting(ClassroomSession::getSessionDate)
        .containsExactly(
            LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 16));
  }

  @Test
  @DisplayName("starts generating from the first Monday on or after recurrence start")
  void generateSessionsFromRecurrence_startsMidNovember_skipsEarlierMondays() {
    ClassroomScheduleRecurrence recurrence =
        classroomScheduleRecurrence().setRecurrenceStartDate(LocalDate.of(2026, 11, 11));
    stubActiveClassroomAndRecurrence(recurrence);

    service.generateSessionsFromRecurrence(RUN_DATE);

    assertThat(capturedInserts())
        .extracting(ClassroomSession::getSessionDate)
        .containsExactly(
            LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 30));
  }

  @Test
  @DisplayName("inserts nothing when there are no active classrooms")
  void generateSessionsFromRecurrence_noClassrooms_insertsEmpty() {
    when(classroomRepository.query(any(BaseQuery.class))).thenReturn(Stream.empty());
    when(classroomScheduleRecurrenceRepository.findActiveByClassroomIdsAsOfDate(
            any(UUID[].class), eq(NOV_1), eq(NOV_30)))
        .thenReturn(List.of());

    service.generateSessionsFromRecurrence(RUN_DATE);

    ArgumentCaptor<UUID[]> idsCaptor = ArgumentCaptor.forClass(UUID[].class);
    verify(classroomScheduleRecurrenceRepository)
        .findActiveByClassroomIdsAsOfDate(idsCaptor.capture(), eq(NOV_1), eq(NOV_30));
    assertThat(idsCaptor.getValue()).isEmpty();
    assertThat(capturedInserts()).isEmpty();
  }

  @Test
  @DisplayName("generates sessions for one active classroom in the given window")
  void generateSessionsForClassroom_activeClassroom_insertsMondaysInWindow() {
    stubClassroomQuery(classroom());
    when(classroomScheduleRecurrenceRepository.findActiveByClassroomIdsAsOfDate(
            any(UUID[].class), eq(NOV_1), eq(NOV_30)))
        .thenReturn(List.of(classroomScheduleRecurrence()));

    service.generateSessionsForClassroom(CLASSROOM_ID, NOV_1, NOV_30);

    ArgumentCaptor<UUID[]> idsCaptor = ArgumentCaptor.forClass(UUID[].class);
    verify(classroomScheduleRecurrenceRepository)
        .findActiveByClassroomIdsAsOfDate(idsCaptor.capture(), eq(NOV_1), eq(NOV_30));
    assertThat(idsCaptor.getValue()).containsExactly(CLASSROOM_ID);
    assertThat(capturedInserts())
        .extracting(ClassroomSession::getSessionDate)
        .containsExactly(
            LocalDate.of(2026, 11, 2),
            LocalDate.of(2026, 11, 9),
            LocalDate.of(2026, 11, 16),
            LocalDate.of(2026, 11, 23),
            LocalDate.of(2026, 11, 30));
  }

  @Test
  @DisplayName("uses the caller window, not the month after runDate")
  void generateSessionsForClassroom_decemberWindow_insertsDecemberMondays() {
    LocalDate dec1 = LocalDate.of(2026, 12, 1);
    LocalDate dec31 = LocalDate.of(2026, 12, 31);
    stubClassroomQuery(classroom());
    when(classroomScheduleRecurrenceRepository.findActiveByClassroomIdsAsOfDate(
            any(UUID[].class), eq(dec1), eq(dec31)))
        .thenReturn(List.of(classroomScheduleRecurrence()));

    service.generateSessionsForClassroom(CLASSROOM_ID, dec1, dec31);

    assertThat(capturedInserts())
        .extracting(ClassroomSession::getSessionDate)
        .containsExactly(
            LocalDate.of(2026, 12, 7),
            LocalDate.of(2026, 12, 14),
            LocalDate.of(2026, 12, 21),
            LocalDate.of(2026, 12, 28));
  }

  @Test
  @DisplayName("throws when the classroom is missing or not ACTIVE")
  void generateSessionsForClassroom_missingClassroom_throwsNotFound() {
    stubClassroomQuery();

    assertThatThrownBy(() -> service.generateSessionsForClassroom(CLASSROOM_ID, NOV_1, NOV_30))
        .isInstanceOf(ObjectNotFoundException.class)
        .hasMessageContaining(CLASSROOM_ID.toString());

    verify(classroomScheduleRecurrenceRepository, never())
        .findActiveByClassroomIdsAsOfDate(
            any(UUID[].class), any(LocalDate.class), any(LocalDate.class));
    verify(classroomSessionRepository, never()).insertAllIgnoringDuplicates(any());
  }

  private void stubActiveClassroomAndRecurrence(ClassroomScheduleRecurrence recurrence) {
    stubClassroomQuery(classroom());
    when(classroomScheduleRecurrenceRepository.findActiveByClassroomIdsAsOfDate(
            any(UUID[].class), eq(NOV_1), eq(NOV_30)))
        .thenReturn(List.of(recurrence));
  }

  private void stubClassroomQuery(Classroom... classrooms) {
    when(classroomRepository.query(any(BaseQuery.class))).thenReturn(Stream.of(classrooms));
  }

  @SuppressWarnings("unchecked")
  private List<ClassroomSession> capturedInserts() {
    ArgumentCaptor<List<ClassroomSession>> captor = ArgumentCaptor.forClass(List.class);
    verify(classroomSessionRepository).insertAllIgnoringDuplicates(captor.capture());
    return captor.getValue();
  }
}
