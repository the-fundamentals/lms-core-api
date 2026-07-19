package tech.sangdang.lmscoreapi.modules.management.app.mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.generated.model.ClassroomFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomResponse;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;

@Mapper(componentModel = "spring")
public interface ClassroomMapper {

  ClassroomResponse toResponse(Classroom classroom);

  BaseQuery toBaseQuery(ClassroomFilter apiFilter);

  default OffsetDateTime map(LocalDateTime value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }
}
