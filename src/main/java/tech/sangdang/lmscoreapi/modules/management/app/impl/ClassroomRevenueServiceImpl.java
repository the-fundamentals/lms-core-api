package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.ClassroomRevenueResponse;
import tech.sangdang.lmscoreapi.generated.model.GetClassroomRevenueQuery;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomRevenueService;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomSessionRepository;

@Service
@RequiredArgsConstructor
public class ClassroomRevenueServiceImpl implements ClassroomRevenueService {

  private static final String CURRENCY = "VND";

  private final ClassroomRepository classroomRepository;
  private final ClassroomSessionRepository classroomSessionRepository;

  @Override
  @Transactional(readOnly = true)
  public ClassroomRevenueResponse getClassroomRevenue(
      UUID classroomId, GetClassroomRevenueQuery query) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    LocalDateTime from = query.getFrom().toLocalDateTime();
    LocalDateTime to = query.getTo().toLocalDateTime();
    if (!from.isBefore(to)) {
      throw GenericBadRequestException.of("INVALID_REVENUE_RANGE", "from must be before to");
    }

    long revenue = classroomSessionRepository.sumRevenue(classroomId, from, to);
    return ClassroomRevenueResponse.builder().revenue(revenue).currency(CURRENCY).build();
  }
}
