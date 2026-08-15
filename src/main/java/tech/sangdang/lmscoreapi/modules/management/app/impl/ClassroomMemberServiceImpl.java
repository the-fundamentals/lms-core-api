package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.ConflictException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.common.querying.QueryFilterConditions;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomMemberRoleCommand;
import tech.sangdang.lmscoreapi.modules.account.dom.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.dom.repository.AccountProfileRepository;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomMemberService;
import tech.sangdang.lmscoreapi.modules.management.app.internal.ClassroomRecordService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMemberMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberRole;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;

@Service
@RequiredArgsConstructor
public class ClassroomMemberServiceImpl implements ClassroomMemberService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomMemberRepository classroomMemberRepository;
  private final ClassroomMemberMapper classroomMemberMapper;
  private final AccountProfileRepository accountProfileRepository;
  private final ClassroomRecordService classroomRecordService;

  @Override
  @Transactional
  public ClassroomMemberResponse createClassroomMember(
      UUID classroomId, CreateClassroomMemberCommand command) {
    // Ensure the classroom exists
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    // Load the account profile used to denormalize name/email
    UUID accountProfileId = command.getAccountId();
    AccountProfile profile =
        accountProfileRepository
            .findById(accountProfileId)
            .orElseThrow(() -> ObjectNotFoundException.of(AccountProfile.class, accountProfileId));

    ClassroomMemberRole role = ClassroomMemberRole.valueOf(command.getRole().getValue());
    String displayName = profile.getFirstName() + " " + profile.getLastName();
    String accountId = accountProfileId.toString();

    var existing = classroomMemberRepository.findByClassroomIdAndAccountId(classroomId, accountId);

    if (existing.isPresent()) {
      ClassroomMember member = existing.get();
      if (member.getStatus() == ClassroomMemberStatus.ACTIVE) {
        throw ConflictException.of(
            "CLASSROOM_MEMBER_ALREADY_EXISTS",
            "Classroom member already active for account: " + member.getAccountId());
      }
      // Reactivate a previously removed member
      member.setRole(role);
      member.setStatus(ClassroomMemberStatus.ACTIVE);
      member.setEmail(profile.getEmail());
      member.setName(displayName);
      ClassroomMember saved = classroomMemberRepository.update(member);
      classroomRecordService.adjustNumberOfMembers(classroomId, 1);
      return classroomMemberMapper.toResponse(saved);
    }

    // Insert a new active membership
    ClassroomMember member =
        new ClassroomMember()
            .setClassroomId(classroomId)
            .setAccountId(accountId)
            .setRole(role)
            .setStatus(ClassroomMemberStatus.ACTIVE)
            .setEmail(profile.getEmail())
            .setName(displayName);
    ClassroomMember saved = classroomMemberRepository.insert(member);

    // Update the classroom record
    classroomRecordService.adjustNumberOfMembers(classroomId, 1);
    return classroomMemberMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public ClassroomMemberResponse updateClassroomMemberRole(
      UUID classroomId, UUID memberId, UpdateClassroomMemberRoleCommand command) {
    ClassroomMember member =
        classroomMemberRepository
            .findById(memberId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomMember.class, memberId));

    // Hide members that belong to another classroom or were already removed
    if (!classroomId.equals(member.getClassroomId())
        || member.getStatus() == ClassroomMemberStatus.REMOVED) {
      throw ObjectNotFoundException.of(ClassroomMember.class, memberId);
    }

    member.setRole(ClassroomMemberRole.valueOf(command.getRole().getValue()));
    return classroomMemberMapper.toResponse(classroomMemberRepository.update(member));
  }

  @Override
  @Transactional
  public void removeClassroomMember(UUID classroomId, UUID memberId) {
    ClassroomMember member =
        classroomMemberRepository
            .findById(memberId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomMember.class, memberId));

    // Hide members that belong to another classroom or were already removed
    if (!classroomId.equals(member.getClassroomId())
        || member.getStatus() == ClassroomMemberStatus.REMOVED) {
      throw ObjectNotFoundException.of(ClassroomMember.class, memberId);
    }

    // Soft-delete and decrement the classroom member count
    member.setStatus(ClassroomMemberStatus.REMOVED);
    classroomMemberRepository.update(member);
    classroomRecordService.adjustNumberOfMembers(classroomId, -1);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomMemberResponse> queryClassroomMembers(
      UUID classroomId, ClassroomMemberFilter filter) {
    // Ensure the classroom exists
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    BaseQuery query = classroomMemberMapper.toBaseQuery(filter);
    List<QueryFilterConditions> filters =
        query.getFilters() == null ? new ArrayList<>() : new ArrayList<>(query.getFilters());

    // Scope the query to this classroom
    filters.add(QueryFilterConditions.of("classroomId", "eq", classroomId.toString()));
    query.setFilters(filters);

    return classroomMemberRepository.query(query).map(classroomMemberMapper::toResponse).toList();
  }
}
