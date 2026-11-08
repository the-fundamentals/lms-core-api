package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberPaymentPlanResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberPaymentPlanCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomMemberPaymentPlanService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMemberPaymentPlanMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberPaymentPlan;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberRole;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.PaymentPlanType;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberPaymentPlanRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;

@Service
@RequiredArgsConstructor
public class ClassroomMemberPaymentPlanServiceImpl implements ClassroomMemberPaymentPlanService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomMemberRepository classroomMemberRepository;
  private final ClassroomMemberPaymentPlanRepository classroomMemberPaymentPlanRepository;
  private final ClassroomMemberPaymentPlanMapper classroomMemberPaymentPlanMapper;

  @Override
  @Transactional
  public ClassroomMemberPaymentPlanResponse createClassroomMemberPaymentPlan(
      UUID classroomId, UUID memberId, CreateClassroomMemberPaymentPlanCommand command) {
    ClassroomMember member = requireMemberInClassroom(classroomId, memberId);
    if (member.getRole() != ClassroomMemberRole.STUDENT
        || member.getStatus() != ClassroomMemberStatus.ACTIVE) {
      throw GenericBadRequestException.of(
          "CLASSROOM_MEMBER_PAYMENT_PLAN_NOT_ALLOWED",
          "Payment plans can only be created for an active student");
    }

    ClassroomMemberPaymentPlan plan =
        replaceCurrent(
            memberId,
            PaymentPlanType.valueOf(command.getType().getValue()),
            command.getAmount(),
            false);

    return classroomMemberPaymentPlanMapper.toResponse(plan);
  }

  @Override
  @Transactional
  public void applyClassroomBaseToActiveStudents(
      UUID classroomId, PaymentPlanType type, long amount) {
    for (ClassroomMemberPaymentPlan current :
        classroomMemberPaymentPlanRepository.findCurrentBasePlansForActiveStudents(classroomId)) {
      replaceCurrent(current.getClassroomMemberId(), type, amount, true);
    }
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

  /**
   * Closes the member's open plan, if any, and inserts the next one at the same time.
   */
  private ClassroomMemberPaymentPlan replaceCurrent(
      UUID memberId, PaymentPlanType type, Long amount, boolean fromClassroomBase) {
    LocalDateTime now = LocalDateTime.now();
    classroomMemberPaymentPlanRepository
        .findCurrent(memberId)
        .ifPresent(
            current -> {
              current.setEffectiveTo(now);
              classroomMemberPaymentPlanRepository.update(current);
            });
    return classroomMemberPaymentPlanRepository.insert(
        new ClassroomMemberPaymentPlan()
            .setClassroomMemberId(memberId)
            .setType(type)
            .setAmount(amount)
            .setEffectiveFrom(now)
            .setEffectiveTo(null)
            .setFromClassroomBase(fromClassroomBase));
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
