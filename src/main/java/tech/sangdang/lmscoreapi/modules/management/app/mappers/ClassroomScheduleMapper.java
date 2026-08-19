package tech.sangdang.lmscoreapi.modules.management.app.mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import tech.sangdang.lmscoreapi.generated.model.ClassroomScheduleResponse;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSchedule;

@Mapper(componentModel = "spring")
public interface ClassroomScheduleMapper {

  ClassroomScheduleResponse toResponse(ClassroomSchedule schedule);

  default OffsetDateTime map(LocalDateTime value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }
}
