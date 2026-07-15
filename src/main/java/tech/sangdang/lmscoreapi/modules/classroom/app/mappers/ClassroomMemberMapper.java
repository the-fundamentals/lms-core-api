package tech.sangdang.lmscoreapi.modules.classroom.app.mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberResponse;
import tech.sangdang.lmscoreapi.modules.classroom.dom.ClassroomMember;

@Mapper(componentModel = "spring")
public interface ClassroomMemberMapper {

  ClassroomMemberResponse toResponse(ClassroomMember member);

  BaseQuery toBaseQuery(ClassroomMemberFilter apiFilter);

  default OffsetDateTime map(LocalDateTime value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }
}
