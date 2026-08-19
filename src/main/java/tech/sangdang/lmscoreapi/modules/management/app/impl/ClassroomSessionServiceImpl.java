package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import tech.sangdang.lmscoreapi.common.exception.ConflictException;
import tech.sangdang.lmscoreapi.common.exception.GenericBadRequestException;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.common.querying.QueryFilterConditions;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceResponse;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendanceCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendancesCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionCommand;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomSessionService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomSessionAttendanceMapper;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomSessionMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendance;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendanceStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomSessionAttendanceRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomSessionRepository;

@Service
@RequiredArgsConstructor
public class ClassroomSessionServiceImpl implements ClassroomSessionService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomSessionRepository classroomSessionRepository;
  private final ClassroomMemberRepository classroomMemberRepository;
  private final ClassroomSessionAttendanceRepository classroomSessionAttendanceRepository;
  private final ClassroomSessionMapper classroomSessionMapper;
  private final ClassroomSessionAttendanceMapper classroomSessionAttendanceMapper;

  @Override
  @Transactional
  public ClassroomSessionResponse createClassroomSession(
      UUID classroomId, CreateClassroomSessionCommand command) {
    // check classroom exists
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    ClassroomSession session =
        new ClassroomSession()
            .setClassroomId(classroomId)
            .setSessionDate(command.getSessionDate().toLocalDateTime())
            .setName(command.getName())
            .setDescription(command.getDescription());
    return classroomSessionMapper.toResponse(classroomSessionRepository.insert(session));
  }

  @Override
  @Transactional(readOnly = true)
  public ClassroomSessionResponse getClassroomSessionById(UUID classroomId, UUID sessionId) {
    return classroomSessionMapper.toResponse(requireSessionInClassroom(classroomId, sessionId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomSessionResponse> queryClassroomSessions(
      UUID classroomId, ClassroomSessionFilter filter) {
    // check classroom exists
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    BaseQuery query = classroomSessionMapper.toBaseQuery(filter);
    List<QueryFilterConditions> filters =
        query.getFilters() == null ? new ArrayList<>() : new ArrayList<>(query.getFilters());

    // scope query to this classroom
    filters.add(QueryFilterConditions.of("classroomId", "eq", classroomId.toString()));
    query.setFilters(filters);

    return classroomSessionRepository.query(query).map(classroomSessionMapper::toResponse).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomSessionAttendanceResponse> queryClassroomSessionAttendancesByMember(
      UUID classroomId, UUID memberId, ClassroomSessionAttendanceFilter filter) {
    // check classroom exists
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));
    // check member exists in classroom
    requireMemberInClassroom(classroomId, memberId);

    BaseQuery query = classroomSessionAttendanceMapper.toBaseQuery(filter);
    List<QueryFilterConditions> filters =
        query.getFilters() == null ? new ArrayList<>() : new ArrayList<>(query.getFilters());

    // scope query to this member
    filters.add(QueryFilterConditions.of("classroomMemberId", "eq", memberId.toString()));
    query.setFilters(filters);

    return classroomSessionAttendanceRepository
        .query(query)
        .map(classroomSessionAttendanceMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void deleteClassroomSession(UUID classroomId, UUID sessionId) {
    // TODO: replace hard delete with soft-delete (status/tombstone) when session lifecycle is
    // finalized; cascade currently removes attendances via FK ON DELETE CASCADE.
    // check session exists in classroom
    ClassroomSession session = requireSessionInClassroom(classroomId, sessionId);
    classroomSessionRepository.deleteById(session.getId());
  }

  @Override
  @Transactional
  public List<ClassroomSessionAttendanceResponse> createClassroomSessionAttendances(
      UUID classroomId, UUID sessionId, CreateClassroomSessionAttendancesCommand command) {
    // check session exists in classroom
    ClassroomSession session = requireSessionInClassroom(classroomId, sessionId);
    List<CreateClassroomSessionAttendanceCommand> items = command.getAttendances();

    // reject duplicate member ids in request
    Set<UUID> seenMemberIds = new HashSet<>();
    List<UUID> memberIds = new ArrayList<>(items.size());
    for (CreateClassroomSessionAttendanceCommand item : items) {
      UUID memberId = item.getClassroomMemberId();
      if (!seenMemberIds.add(memberId)) {
        throw GenericBadRequestException.of(
            "DUPLICATE_CLASSROOM_MEMBER_ID",
            "Duplicate classroom member id in request: " + memberId);
      }
      memberIds.add(memberId);
    }

    // check member ids exist in the classroom
    Map<UUID, ClassroomMember> membersById =
        classroomMemberRepository.findAllById(memberIds).stream()
            .collect(Collectors.toMap(ClassroomMember::getId, Function.identity()));
    for (UUID memberId : memberIds) {
      ClassroomMember member = membersById.get(memberId);
      if (member == null || !classroomId.equals(member.getClassroomId())) {
        throw ObjectNotFoundException.of(ClassroomMember.class, memberId);
      }
    }

    // reject members that already have attendance in this session
    Map<UUID, ClassroomSessionAttendance> existingByMemberId =
        classroomSessionAttendanceRepository
            .findBySessionIdAndClassroomMemberIdIn(session.getId(), memberIds)
            .stream()
            .collect(
                Collectors.toMap(
                    ClassroomSessionAttendance::getClassroomMemberId, Function.identity()));
    for (UUID memberId : memberIds) {
      if (existingByMemberId.containsKey(memberId)) {
        throw ConflictException.of(
            "CLASSROOM_SESSION_ATTENDANCE_ALREADY_EXISTS",
            "Attendance already exists for member in session: " + memberId);
      }
    }

    // insert attendance records
    LocalDateTime now = LocalDateTime.now();
    List<ClassroomSessionAttendance> toInsert = new ArrayList<>(items.size());
    for (CreateClassroomSessionAttendanceCommand item : items) {
      LocalDateTime attendanceDate =
          item.getAttendanceDate() != null ? item.getAttendanceDate().toLocalDateTime() : now;
      toInsert.add(
          new ClassroomSessionAttendance()
              .setSessionId(session.getId())
              .setClassroomMemberId(item.getClassroomMemberId())
              .setAttendanceDate(attendanceDate)
              .setStatus(ClassroomSessionAttendanceStatus.valueOf(item.getStatus().getValue())));
    }

    Map<UUID, ClassroomSessionAttendance> savedByMemberId =
        classroomSessionAttendanceRepository.insertAll(toInsert).stream()
            .collect(
                Collectors.toMap(
                    ClassroomSessionAttendance::getClassroomMemberId, Function.identity()));

    // return in request order
    return items.stream()
        .map(item -> savedByMemberId.get(item.getClassroomMemberId()))
        .map(classroomSessionAttendanceMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void deleteClassroomSessionAttendance(
      UUID classroomId, UUID sessionId, UUID attendanceId) {
    // check session exists in classroom
    requireSessionInClassroom(classroomId, sessionId);

    // check attendance exists on this session
    ClassroomSessionAttendance attendance =
        classroomSessionAttendanceRepository
            .findById(attendanceId)
            .orElseThrow(
                () -> ObjectNotFoundException.of(ClassroomSessionAttendance.class, attendanceId));

    if (!sessionId.equals(attendance.getSessionId())) {
      throw ObjectNotFoundException.of(ClassroomSessionAttendance.class, attendanceId);
    }

    classroomSessionAttendanceRepository.deleteById(attendanceId);
  }

  private ClassroomSession requireSessionInClassroom(UUID classroomId, UUID sessionId) {
    ClassroomSession session =
        classroomSessionRepository
            .findById(sessionId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomSession.class, sessionId));

    if (!classroomId.equals(session.getClassroomId())) {
      throw ObjectNotFoundException.of(ClassroomSession.class, sessionId);
    }
    return session;
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
