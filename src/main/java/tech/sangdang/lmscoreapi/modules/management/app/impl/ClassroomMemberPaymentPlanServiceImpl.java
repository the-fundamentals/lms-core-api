package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.ConflictException;
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

  private static final Duration DELETE_WINDOW = Duration.ofMinutes(5);
  private static final String DEFAULT_CURRENCY = "VND";

  private final ClassroomRepository classroomRepository;
  private final ClassroomMemberRepository classroomMemberRepository;
  private final ClassroomMemberPaymentPlanRepository classroomMemberPaymentPlanRepository;
  private final ClassroomMemberPaymentPlanMapper classroomMemberPaymentPlanMapper;

  @Override
  @Transactional
  public ClassroomMemberPaymentPlanResponse createClassroomMemberPaymentPlan(
      UUID classroomId, UUID memberId, CreateClassroomMemberPaymentPlanCommand command) {
    // check member exists in classroom
    requireMemberInClassroom(classroomId, memberId);

    Instant now = Instant.now();

    // replace current plan (if any) and insert the new current plan
    classroomMemberPaymentPlanRepository
        .findCurrent(memberId)
        .ifPresent(
            current -> {
              current.setIsCurrent(false);
              current.setReplacedAt(now);
              classroomMemberPaymentPlanRepository.update(current);
            });

    ClassroomMemberPaymentPlan plan =
        new ClassroomMemberPaymentPlan()
            .setClassroomMemberId(memberId)
            .setType(PaymentPlanType.valueOf(command.getType().getValue()))
            .setAmount(command.getAmount())
            .setCurrency(command.getCurrency().isBlank() ? DEFAULT_CURRENCY : command.getCurrency())
            .setIsCurrent(true)
            .setReplacedAt(null);

    return classroomMemberPaymentPlanMapper.toResponse(
        classroomMemberPaymentPlanRepository.insert(plan));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomMemberPaymentPlanResponse> getClassroomMemberPaymentPlans(
      UUID classroomId, UUID memberId) {
    // check member exists in classroom
    requireMemberInClassroom(classroomId, memberId);

    // load plans for the member (current first)
    return classroomMemberPaymentPlanRepository.findByMember(memberId).stream()
        .map(classroomMemberPaymentPlanMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomMemberPaymentPlanResponse> getAllClassroomPaymentPlans(UUID classroomId) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    return classroomMemberPaymentPlanRepository.findByClassroom(classroomId).stream()
        // LEFT JOIN yields a row with a null id when the member has no plan
        .filter(plan -> Objects.nonNull(plan.getId()))
        .map(classroomMemberPaymentPlanMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void deleteClassroomMemberPaymentPlan(
      UUID classroomId, UUID memberId, UUID paymentPlanId) {
    // check member exists in classroom
    requireMemberInClassroom(classroomId, memberId);

    ClassroomMemberPaymentPlan plan =
        classroomMemberPaymentPlanRepository
            .findById(paymentPlanId)
            .orElseThrow(
                () -> ObjectNotFoundException.of(ClassroomMemberPaymentPlan.class, paymentPlanId));

    if (!memberId.equals(plan.getClassroomMemberId())) {
      throw ObjectNotFoundException.of(ClassroomMemberPaymentPlan.class, paymentPlanId);
    }

    Instant createdDate = plan.getCreatedDate();
    if (createdDate == null || createdDate.isBefore(Instant.now().minus(DELETE_WINDOW))) {
      throw ConflictException.of(
          "CLASSROOM_MEMBER_PAYMENT_PLAN_DELETE_WINDOW_EXPIRED",
          "Payment plan can only be deleted within 5 minutes of creation");
    }

    // delete only when still inside the creation window
    classroomMemberPaymentPlanRepository.deleteById(paymentPlanId);
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
