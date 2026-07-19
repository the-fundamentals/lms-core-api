package tech.sangdang.lmscoreapi.modules.management.app.mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import tech.sangdang.lmscoreapi.generated.model.ClassroomSessionAttendanceResponse;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendance;

@Mapper(componentModel = "spring")
public interface ClassroomSessionAttendanceMapper {

  ClassroomSessionAttendanceResponse toResponse(ClassroomSessionAttendance attendance);

  default OffsetDateTime map(LocalDateTime value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }
}
