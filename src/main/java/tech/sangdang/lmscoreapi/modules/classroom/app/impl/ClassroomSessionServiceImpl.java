package tech.sangdang.lmscoreapi.modules.classroom.app.impl;

import java.time.LocalDateTime;
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
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceResponse;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionResponse;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionAttendanceCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomSessionCommand;
import tech.sangdang.lmscoreapi.modules.classroom.app.ClassroomSessionService;
import tech.sangdang.lmscoreapi.modules.classroom.app.mappers.ClassroomSessionAttendanceMapper;
import tech.sangdang.lmscoreapi.modules.classroom.app.mappers.ClassroomSessionMapper;
import tech.sangdang.lmscoreapi.modules.classroom.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.classroom.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.classroom.dom.ClassroomMemberStatus;
import tech.sangdang.lmscoreapi.modules.classroom.dom.ClassroomSession;
import tech.sangdang.lmscoreapi.modules.classroom.dom.ClassroomSessionAttendance;
import tech.sangdang.lmscoreapi.modules.classroom.dom.ClassroomSessionAttendanceStatus;
import tech.sangdang.lmscoreapi.modules.classroom.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.classroom.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.classroom.dom.repository.ClassroomSessionAttendanceRepository;
import tech.sangdang.lmscoreapi.modules.classroom.dom.repository.ClassroomSessionRepository;

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
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    ClassroomSession session =
        new ClassroomSession()
            .setClassroomId(classroomId)
            .setSessionDate(command.getSessionDate().toLocalDateTime());
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
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    BaseQuery query = classroomSessionMapper.toBaseQuery(filter);
    List<QueryFilterConditions> filters =
        query.getFilters() == null ? new ArrayList<>() : new ArrayList<>(query.getFilters());

    filters.add(QueryFilterConditions.of("classroomId", "eq", classroomId.toString()));
    query.setFilters(filters);

    return classroomSessionRepository.query(query).map(classroomSessionMapper::toResponse).toList();
  }

  @Override
  @Transactional
  public void deleteClassroomSession(UUID classroomId, UUID sessionId) {
    // TODO: replace hard delete with soft-delete (status/tombstone) when session lifecycle is
    // finalized; cascade currently removes attendances via FK ON DELETE CASCADE.
    ClassroomSession session = requireSessionInClassroom(classroomId, sessionId);
    classroomSessionRepository.deleteById(session.getId());
  }

  @Override
  @Transactional
  public ClassroomSessionAttendanceResponse createClassroomSessionAttendance(
      UUID classroomId, UUID sessionId, CreateClassroomSessionAttendanceCommand command) {
    ClassroomSession session = requireSessionInClassroom(classroomId, sessionId);
    ClassroomMember member =
        requireActiveMemberInClassroom(classroomId, command.getClassroomMemberId());

    if (classroomSessionAttendanceRepository
        .findBySessionIdAndClassroomMemberId(session.getId(), member.getId())
        .isPresent()) {
      throw ConflictException.of(
          "CLASSROOM_SESSION_ATTENDANCE_ALREADY_EXISTS",
          "Attendance already exists for member in session: " + member.getId());
    }

    LocalDateTime attendanceDate =
        command.getAttendanceDate() != null
            ? command.getAttendanceDate().toLocalDateTime()
            : LocalDateTime.now();

    ClassroomSessionAttendance attendance =
        new ClassroomSessionAttendance()
            .setSessionId(session.getId())
            .setClassroomMemberId(member.getId())
            .setAttendanceDate(attendanceDate)
            .setStatus(ClassroomSessionAttendanceStatus.valueOf(command.getStatus().getValue()));

    return classroomSessionAttendanceMapper.toResponse(
        classroomSessionAttendanceRepository.insert(attendance));
  }

  @Override
  @Transactional
  public void deleteClassroomSessionAttendance(
      UUID classroomId, UUID sessionId, UUID attendanceId) {
    requireSessionInClassroom(classroomId, sessionId);

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

  private ClassroomMember requireActiveMemberInClassroom(UUID classroomId, UUID memberId) {
    ClassroomMember member =
        classroomMemberRepository
            .findById(memberId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomMember.class, memberId));

    if (!classroomId.equals(member.getClassroomId())
        || member.getStatus() == ClassroomMemberStatus.REMOVED) {
      throw ObjectNotFoundException.of(ClassroomMember.class, memberId);
    }
    return member;
  }
}
