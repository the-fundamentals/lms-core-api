package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMembersCommand;
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
  public List<ClassroomMemberResponse> createClassroomMembers(
      UUID classroomId, CreateClassroomMembersCommand command) {

    // check classroom exists
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    List<CreateClassroomMemberCommand> items = command.getMembers();
    Set<UUID> seenAccountIds = new HashSet<>();
    List<UUID> accountProfileIds = new ArrayList<>(items.size());

    // remove duplicate account ids in request
    for (CreateClassroomMemberCommand item : items) {
      UUID accountProfileId = item.getAccountId();
      if (!seenAccountIds.add(accountProfileId)) {
        throw GenericBadRequestException.of(
            "DUPLICATE_ACCOUNT_ID", "Duplicate account id in request: " + accountProfileId);
      }
      accountProfileIds.add(accountProfileId);
    }

    // check all account ids exist in the database
    Map<UUID, AccountProfile> profilesById =
        accountProfileRepository.findAllById(accountProfileIds).stream()
            .collect(Collectors.toMap(AccountProfile::getId, Function.identity()));
    for (UUID accountProfileId : accountProfileIds) {
      if (!profilesById.containsKey(accountProfileId)) {
        throw ObjectNotFoundException.of(AccountProfile.class, accountProfileId);
      }
    }

    List<String> accountIds = accountProfileIds.stream().map(UUID::toString).toList();
    Map<String, ClassroomMember> existingByAccountId =
        classroomMemberRepository.findByClassroomIdAndAccountIdIn(classroomId, accountIds).stream()
            .collect(Collectors.toMap(ClassroomMember::getAccountId, Function.identity()));

    List<ClassroomMember> toInsert = new ArrayList<>();
    List<ClassroomMember> toUpdate = new ArrayList<>();
    int newActiveCount = 0;

    for (CreateClassroomMemberCommand item : items) {
      AccountProfile profile = profilesById.get(item.getAccountId());
      ClassroomMemberRole role = ClassroomMemberRole.valueOf(item.getRole().getValue());
      String displayName = profile.getFirstName() + " " + profile.getLastName();
      String accountId = item.getAccountId().toString();
      ClassroomMember existing = existingByAccountId.get(accountId);

      if (existing != null) {
        boolean wasRemoved = existing.getStatus() == ClassroomMemberStatus.REMOVED;

        existing.setRole(role);
        existing.setStatus(ClassroomMemberStatus.ACTIVE);
        existing.setEmail(profile.getEmail());
        existing.setName(displayName);

        toUpdate.add(existing);
        if (wasRemoved) {
          newActiveCount++;
        }
      } else {
        toInsert.add(
            new ClassroomMember()
                .setClassroomId(classroomId)
                .setAccountId(accountId)
                .setRole(role)
                .setStatus(ClassroomMemberStatus.ACTIVE)
                .setEmail(profile.getEmail())
                .setName(displayName));
        newActiveCount++;
      }
    }

    Map<String, ClassroomMember> savedByAccountId = new HashMap<>(existingByAccountId);
    if (!toUpdate.isEmpty()) {
      for (ClassroomMember saved : classroomMemberRepository.updateAll(toUpdate)) {
        savedByAccountId.put(saved.getAccountId(), saved);
      }
    }
    if (!toInsert.isEmpty()) {
      for (ClassroomMember saved : classroomMemberRepository.insertAll(toInsert)) {
        savedByAccountId.put(saved.getAccountId(), saved);
      }
    }
    if (newActiveCount > 0) {
      classroomRecordService.adjustNumberOfMembers(classroomId, newActiveCount);
    }

    return items.stream()
        .map(item -> savedByAccountId.get(item.getAccountId().toString()))
        .map(classroomMemberMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void removeClassroomMember(UUID classroomId, UUID memberId) {
    ClassroomMember member =
        classroomMemberRepository
            .findById(memberId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomMember.class, memberId));

    if (!classroomId.equals(member.getClassroomId()) || !member.isActive()) {
      throw ObjectNotFoundException.of(ClassroomMember.class, memberId);
    }

    member.setStatus(ClassroomMemberStatus.REMOVED);
    classroomMemberRepository.update(member);
    classroomRecordService.adjustNumberOfMembers(classroomId, -1);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomMemberResponse> getAllClassroomMembers(UUID classroomId) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    return classroomMemberRepository.findByClassroomId(classroomId).stream()
        .map(classroomMemberMapper::toResponse)
        .toList();
  }
}
