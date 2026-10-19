package tech.sangdang.lmscoreapi.modules.management.app.useCase.InvoiceGeneration;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.common.querying.Operators;
import tech.sangdang.lmscoreapi.modules.management.dom.*;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class ClassroomInvoiceGenerationServiceImpl implements ClassroomInvoiceGenerationService {
  private final ClassroomRepository classroomRepository;
  private final ClassroomMemberRepository classroomMemberRepository;
  private final ClassroomSessionAttendanceRepository classroomSessionAttendanceRepository;
  private final ClassroomMemberPaymentPlanRepository classroomMemberPaymentPlanRepository;
  private final ClassroomInvoiceGenerationRepository classroomInvoiceGenerationRepository;

  @Override
  @Transactional
  public void generateInvoices(LocalDateTime startDateTime, LocalDateTime endDateTime) {

    // MVP: fetchAll caps at 10k rows — batch/page if a tenant can exceed that.
    List<UUID> classroomIds =
        classroomRepository
            .query(
                BaseQuery.builder()
                    .fetchAll()
                    .addFilter(Classroom.Fields.status, Operators.EQUAL, ClassroomStatus.ACTIVE)
                    .build())
            .filter(Classroom::isActive) // Apply domain filter
            .map(Classroom::getId)
            .toList();

    if (classroomIds.isEmpty()) {
      log.info(
          "No active classrooms; skipping monthly invoice generation for {} - {}",
          startDateTime,
          endDateTime);
      return;
    }

    List<ClassroomMember> members =
        classroomMemberRepository
            .query(
                BaseQuery.builder()
                    .fetchAll()
                    .addFilter(ClassroomMember.Fields.classroomId, Operators.IN, classroomIds)
                    .addFilter(
                        ClassroomMember.Fields.role, Operators.EQUAL, ClassroomMemberRole.STUDENT)
                    .addFilter(
                        ClassroomMember.Fields.status,
                        Operators.EQUAL,
                        ClassroomMemberStatus.ACTIVE)
                    .build())
            .filter(ClassroomMember::isActive) // Apply domain filter
            .toList();
    List<UUID> memberIds = members.stream().map(ClassroomMember::getId).toList();

    if (memberIds.isEmpty()) {
      log.info(
          "No active classroom members; skipping monthly invoice generation for {} - {}",
          startDateTime,
          endDateTime);
      return;
    }

    Map<UUID, List<ClassroomSessionAttendance>> memberToAttendance =
        classroomSessionAttendanceRepository
            .query(
                BaseQuery.builder()
                    .fetchAll()
                    .addFilter(
                        ClassroomSessionAttendance.Fields.classroomMemberId,
                        Operators.IN,
                        memberIds)
                    .addFilter(
                        ClassroomSessionAttendance.Fields.attendanceDate,
                        Operators.GREATER_OR_EQUAL,
                        startDateTime)
                    .addFilter(
                        ClassroomSessionAttendance.Fields.attendanceDate,
                        Operators.LESS_OR_EQUAL,
                        endDateTime)
                    .addFilter(
                        ClassroomSessionAttendance.Fields.status,
                        Operators.EQUAL,
                        ClassroomSessionAttendanceStatus.ATTENDED)
                    .build())
            .filter(ClassroomSessionAttendance::hasAttended) // Apply domain filter
            .collect(Collectors.groupingBy(ClassroomSessionAttendance::getClassroomMemberId));

    Map<UUID, ClassroomMemberPaymentPlan> memberToCurrentPaymentPlan =
        classroomMemberPaymentPlanRepository.findCurrentByMembers(memberIds).stream()
            .collect(
                Collectors.toMap(
                    ClassroomMemberPaymentPlan::getClassroomMemberId, plan -> plan, (a, b) -> a));

    List<ClassroomInvoice> invoices = new ArrayList<>();
    members.forEach(
        member ->
            invoices.add(
                processMember(
                    member,
                    memberToAttendance.getOrDefault(member.getId(), List.of()),
                    memberToCurrentPaymentPlan.get(member.getId()),
                    startDateTime)));

    classroomInvoiceGenerationRepository.insertAllIgnoringDuplicates(invoices);
  }

  private ClassroomInvoice processMember(
      @NonNull ClassroomMember member,
      @NonNull List<ClassroomSessionAttendance> attendances,
      ClassroomMemberPaymentPlan currentPaymentPlan,
      @NonNull LocalDateTime startDateTime) {
    String description =
        String.format(
            "Attended dates: %s",
            attendances.isEmpty()
                ? "N/A"
                : attendances.stream()
                    .map(attendance -> attendance.getAttendanceDate().toString())
                    .collect(Collectors.joining(", ")));

    Long unitPrice = currentPaymentPlan == null ? 0L : currentPaymentPlan.getAmount();

    ClassroomInvoiceLine generatedLessonsInvoice =
        ClassroomInvoiceLine.ofLesson(description, unitPrice, attendances.size());

    return ClassroomInvoice.ofGenerated(
        member.getClassroomId(),
        member.getId(),
        Set.of(generatedLessonsInvoice),
        startDateTime.toLocalDate(),
        ClassroomInvoicePeriodRange.MONTH,
        String.format(
            "Automatically-generated invoice for student %s during month %s",
            member.getName(), startDateTime.getMonth().toString()));
  }
}
