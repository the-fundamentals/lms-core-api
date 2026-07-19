package tech.sangdang.lmscoreapi.modules.classroom.app.mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionResponse;
import tech.sangdang.lmscoreapi.modules.classroom.dom.ClassroomSession;

@Mapper(componentModel = "spring")
public interface ClassroomSessionMapper {

  ClassroomSessionResponse toResponse(ClassroomSession session);

  BaseQuery toBaseQuery(ClassroomSessionFilter apiFilter);

  default OffsetDateTime map(LocalDateTime value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }
}
