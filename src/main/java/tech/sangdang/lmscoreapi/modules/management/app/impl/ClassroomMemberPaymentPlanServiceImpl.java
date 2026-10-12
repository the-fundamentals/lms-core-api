package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberPaymentPlanResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberPaymentPlanCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomMemberPaymentPlanService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMemberPaymentPlanMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberPaymentPlan;
import tech.sangdang.lmscoreapi.modules.management.dom.PaymentPlanType;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberPaymentPlanRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;

@Service
@RequiredArgsConstructor
public class ClassroomMemberPaymentPlanServiceImpl implements ClassroomMemberPaymentPlanService {

  private static final String DEFAULT_CURRENCY = "VND";

  private final ClassroomRepository classroomRepository;
  private final ClassroomMemberRepository classroomMemberRepository;
  private final ClassroomMemberPaymentPlanRepository classroomMemberPaymentPlanRepository;
  private final ClassroomMemberPaymentPlanMapper classroomMemberPaymentPlanMapper;

  @Override
  @Transactional
  public ClassroomMemberPaymentPlanResponse createClassroomMemberPaymentPlan(
      UUID classroomId, UUID memberId, CreateClassroomMemberPaymentPlanCommand command) {
    requireMemberInClassroom(classroomId, memberId);

    LocalDateTime now = LocalDateTime.now();

    // Close the open-ended plan (if any); new plan starts at the same time — no future scheduling
    classroomMemberPaymentPlanRepository
        .findCurrent(memberId)
        .ifPresent(
            current -> {
              current.setEffectiveTo(now);
              classroomMemberPaymentPlanRepository.update(current);
            });

    ClassroomMemberPaymentPlan plan =
        new ClassroomMemberPaymentPlan()
            .setClassroomMemberId(memberId)
            .setType(PaymentPlanType.valueOf(command.getType().getValue()))
            .setAmount(command.getAmount())
            .setCurrency(resolveCurrency(command.getCurrency()))
            .setEffectiveFrom(now)
            .setEffectiveTo(null);

    return classroomMemberPaymentPlanMapper.toResponse(
        classroomMemberPaymentPlanRepository.insert(plan));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomMemberPaymentPlanResponse> getClassroomMemberPaymentPlans(
      UUID classroomId, UUID memberId) {
    requireMemberInClassroom(classroomId, memberId);

    return classroomMemberPaymentPlanRepository.findByMember(memberId).stream()
        .map(classroomMemberPaymentPlanMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ClassroomMemberPaymentPlanResponse getClassroomMemberPaymentPlanById(
      UUID classroomId, UUID memberId, UUID paymentPlanId) {
    requireMemberInClassroom(classroomId, memberId);

    ClassroomMemberPaymentPlan plan =
        classroomMemberPaymentPlanRepository
            .findById(paymentPlanId)
            .orElseThrow(
                () -> ObjectNotFoundException.of(ClassroomMemberPaymentPlan.class, paymentPlanId));

    if (!memberId.equals(plan.getClassroomMemberId())) {
      throw ObjectNotFoundException.of(ClassroomMemberPaymentPlan.class, paymentPlanId);
    }

    return classroomMemberPaymentPlanMapper.toResponse(plan);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomMemberPaymentPlanResponse> getAllClassroomPaymentPlans(UUID classroomId) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    return classroomMemberPaymentPlanRepository.findByClassroom(classroomId).stream()
        // JDBC may still map an unmatched join as a null list element or a null-id entity
        .filter(plan -> plan != null && plan.getId() != null)
        .map(classroomMemberPaymentPlanMapper::toResponse)
        .toList();
  }

  private static String resolveCurrency(String currency) {
    return currency == null || currency.isBlank() ? DEFAULT_CURRENCY : currency;
  }

  private ClassroomMember requireMemberInClassroom(UUID classroomId, UUID memberId) {
    ClassroomMember member =
        classroomMemberRepository
            .findById(memberId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomMember.class, memberId));
    if (!classroomId.equals(member.getClassroomId())) {
      throw ObjectNotFoundException.of(ClassroomMember.class, memberId);
    }
    return member;
  }
}
