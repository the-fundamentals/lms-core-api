package tech.sangdang.lmscoreapi.modules.management.app.useCase.SessionGeneration;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;

@Repository
@RequiredArgsConstructor
public class ClassroomSessionGenerationRepository {
  private final NamedParameterJdbcTemplate template;

  /**
   * Inserts sessions; skips existing rows for the same classroom, recurrence, and date.
   *
   * <ul>
   *   <li>Conflict target: partial unique index on {@code (classroom_id, generated_by,
   *       session_date)} where {@code generated_by} is not null.
   * </ul>
   *
   * @param sessions sessions to insert (null/empty is a no-op)
   */
  public void insertAllIgnoringDuplicates(List<ClassroomSession> sessions) {
    if (sessions == null) {
      return;
    }
    sessions = sessions.stream().filter(Objects::nonNull).toList();
    if (sessions.isEmpty()) {
      return;
    }

    // Ad-hoc SQL: Criteria cannot express ON CONFLICT on partial unique index
    String sql =
        """
        INSERT INTO classroom_session (
          session_date, start_time, end_time, classroom_id, name, description, status, type, generated_by
        ) VALUES (
          :sessionDate, :startTime, :endTime, :classroomId, :name, :description, :status, :type, :generatedBy
        )
        ON CONFLICT (classroom_id, generated_by, session_date) WHERE generated_by IS NOT NULL
        DO NOTHING
        """;

    SqlParameterSource[] batchArgs =
        sessions.stream()
            .map(
                s ->
                    new MapSqlParameterSource()
                        .addValue("sessionDate", s.getSessionDate())
                        .addValue("startTime", s.getStartTime())
                        .addValue("endTime", s.getEndTime())
                        .addValue("classroomId", s.getClassroomId())
                        .addValue("name", s.getName())
                        .addValue("description", s.getDescription())
                        .addValue("status", s.getStatus().name())
                        .addValue("type", s.getType().name())
                        .addValue("generatedBy", s.getGeneratedBy()))
            .toArray(SqlParameterSource[]::new);

    template.batchUpdate(sql, batchArgs);
  }
}
