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
import tech.sangdang.lmscoreapi.modules.account.infra.AccountProfile;
import tech.sangdang.lmscoreapi.modules.account.infra.AccountProfileCache;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomMemberService;
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
  private final AccountProfileCache accountProfileCache;

  @Override
  @Transactional
  public ClassroomMemberResponse createClassroomMember(
      UUID classroomId, CreateClassroomMemberCommand command) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    AccountProfile profile =
        accountProfileCache
            .findByAccountId(command.getAccountId())
            .orElseThrow(
                () -> ObjectNotFoundException.of(AccountProfile.class, command.getAccountId()));

    ClassroomMemberRole role = ClassroomMemberRole.valueOf(command.getRole().getValue());

    var existing =
        classroomMemberRepository.findByClassroomIdAndAccountId(
            classroomId, command.getAccountId());

    if (existing.isPresent()) {
      ClassroomMember member = existing.get();
      if (member.getStatus() == ClassroomMemberStatus.ACTIVE) {
        throw ConflictException.of(
            "CLASSROOM_MEMBER_ALREADY_EXISTS",
            "Classroom member already active for account: " + member.getAccountId());
      }
      member.setRole(role);
      member.setStatus(ClassroomMemberStatus.ACTIVE);
      member.setEmail(profile.email());
      member.setName(profile.name());
      return classroomMemberMapper.toResponse(classroomMemberRepository.update(member));
    }

    ClassroomMember member =
        new ClassroomMember()
            .setClassroomId(classroomId)
            .setAccountId(command.getAccountId())
            .setRole(role)
            .setStatus(ClassroomMemberStatus.ACTIVE)
            .setEmail(profile.email())
            .setName(profile.name());
    return classroomMemberMapper.toResponse(classroomMemberRepository.insert(member));
  }

  @Override
  @Transactional
  public ClassroomMemberResponse updateClassroomMemberRole(
      UUID classroomId, UUID memberId, UpdateClassroomMemberRoleCommand command) {
    ClassroomMember member =
        classroomMemberRepository
            .findById(memberId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomMember.class, memberId));

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

    if (!classroomId.equals(member.getClassroomId())
        || member.getStatus() == ClassroomMemberStatus.REMOVED) {
      throw ObjectNotFoundException.of(ClassroomMember.class, memberId);
    }

    member.setStatus(ClassroomMemberStatus.REMOVED);
    classroomMemberRepository.update(member);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomMemberResponse> queryClassroomMembers(
      UUID classroomId, ClassroomMemberFilter filter) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    BaseQuery query = classroomMemberMapper.toBaseQuery(filter);
    List<QueryFilterConditions> filters =
        query.getFilters() == null ? new ArrayList<>() : new ArrayList<>(query.getFilters());

    filters.add(QueryFilterConditions.of("classroomId", "eq", classroomId.toString()));
    query.setFilters(filters);

    return classroomMemberRepository.query(query).map(classroomMemberMapper::toResponse).toList();
  }
}
