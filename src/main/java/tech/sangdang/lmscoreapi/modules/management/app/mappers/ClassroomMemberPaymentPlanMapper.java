package tech.sangdang.lmscoreapi.modules.management.app.mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import tech.sangdang.lmscoreapi.generated.model.ClassroomMemberPaymentPlanResponse;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberPaymentPlan;

@Mapper(componentModel = "spring")
public interface ClassroomMemberPaymentPlanMapper {

  ClassroomMemberPaymentPlanResponse toResponse(ClassroomMemberPaymentPlan plan);

  default OffsetDateTime map(Instant value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }
}
