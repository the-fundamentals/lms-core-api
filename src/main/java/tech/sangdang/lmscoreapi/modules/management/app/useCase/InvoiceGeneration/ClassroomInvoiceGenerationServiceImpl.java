package tech.sangdang.lmscoreapi.modules.management.app.useCase.InvoiceGeneration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.common.querying.Operators;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;

import java.time.Month;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class ClassroomInvoiceGenerationServiceImpl implements ClassroomInvoiceGenerationService {
  private final ClassroomRepository classroomRepository;
  private final ClassroomMemberRepository classroomMemberRepository;

  @Override
  public void generateMonthlyInvoices(Month month) {

    // Fetch all active classrooms
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

    // Fetch all active classroom members in these classrooms
    classroomMemberRepository.query(
        BaseQuery.builder()
            .fetchAll()
            .addFilter(ClassroomMember.Fields.classroomId, Operators.IN, classroomIds)
            .build());

    // Fetch all classroom members' attendances in this month

    // Fetch all classroom members' payment plans for this month

    // Combine into invoices & save

  }
}
