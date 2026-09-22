package tech.sangdang.lmscoreapi.modules.management.app.mappers;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionResponse;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;

@Mapper(componentModel = "spring")
public interface ClassroomSessionMapper {

  ClassroomSessionResponse toResponse(ClassroomSession session);

  BaseQuery toBaseQuery(ClassroomSessionFilter apiFilter);

  default OffsetDateTime map(LocalDateTime value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }

  // OpenAPI generator emits format:time as String, not LocalTime
  default String map(LocalTime value) {
    return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_TIME);
  }
}
