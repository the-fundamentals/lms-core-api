package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.common.querying.Operators;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomSessionGenerationService;
import tech.sangdang.lmscoreapi.modules.management.app.internal.RecurrenceRuleUtils;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomScheduleRecurrence;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomScheduleRecurrenceRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomSessionRepository;

@Service
@RequiredArgsConstructor
public class ClassroomSessionGenerationServiceImpl implements ClassroomSessionGenerationService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomSessionRepository classroomSessionRepository;
  private final ClassroomScheduleRecurrenceRepository classroomScheduleRecurrenceRepository;

  @Override
  @Transactional
  public void generateSessionsFromRecurrence(LocalDate runDate) {
    // Generates the next calendar month of classroom sessions (not the month of runDate)
    List<UUID> classroomIds =
        classroomRepository
            .query(
                BaseQuery.builder()
                    .fetchAll()
                    .addFilter(Classroom.Fields.status, Operators.EQUAL, ClassroomStatus.ACTIVE)
                    .build())
            .filter(Classroom::isActive)
            .map(Classroom::getId)
            .toList();

    // Recurrences that overlap that next month
    LocalDate startDate = runDate.plusMonths(1).withDayOfMonth(1);
    LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

    List<ClassroomScheduleRecurrence> recurrences =
        classroomScheduleRecurrenceRepository.findActiveByClassroomIdsAsOfDate(
            classroomIds.toArray(UUID[]::new), startDate, endDate);
    generateSessions(recurrences, startDate, endDate);
  }

  @Override
  @Transactional
  public void generateSessionsForClassroom(
      UUID classroomId, LocalDate startDate, LocalDate endDate) {
    Classroom classroom =
        classroomRepository
            .query(
                BaseQuery.builder()
                    .fetchFirst()
                    .addFilter(Classroom.Fields.status, Operators.EQUAL, ClassroomStatus.ACTIVE)
                    .addFilter(Classroom.Fields.id, Operators.EQUAL, classroomId.toString())
                    .build())
            .findFirst()
            // ACTIVE-only query: missing and inactive both look like not found
            .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    List<ClassroomScheduleRecurrence> recurrences =
        classroomScheduleRecurrenceRepository.findActiveByClassroomIdsAsOfDate(
            new UUID[] {classroom.getId()}, startDate, endDate);

    generateSessions(recurrences, startDate, endDate);
  }

  private void generateSessions(
      List<ClassroomScheduleRecurrence> recurrences, LocalDate startDate, LocalDate endDate) {
    List<ClassroomSession> sessionsToCreate = new ArrayList<>();
    recurrences.forEach(
        recurrence -> {
          List<LocalDate> dates =
              RecurrenceRuleUtils.expandRecurrenceRule(recurrence, startDate, endDate);
          dates.forEach(
              date ->
                  sessionsToCreate.add(
                      ClassroomSession.fromScheduleRecurrence(
                          date,
                          recurrence.getStartTime(),
                          recurrence.getEndTime(),
                          recurrence.getClassroomId(),
                          "Generated From Schedule",
                          "This was automatically generated from a classroom schedule",
                          recurrence.getId())));
        });

    classroomSessionRepository.insertAllIgnoringDuplicates(sessionsToCreate);
  }
}
