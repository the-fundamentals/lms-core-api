package tech.sangdang.lmscoreapi.modules.management.app.mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import tech.sangdang.lmscoreapi.common.querying.QueryMapper;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceLineResponse;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceResponse;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoice;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceLine;

@Mapper(componentModel = "spring")
public interface ClassroomInvoiceMapper extends QueryMapper<ClassroomInvoiceFilter> {

  ClassroomInvoiceResponse toResponse(ClassroomInvoice invoice);

  ClassroomInvoiceLineResponse toLineResponse(ClassroomInvoiceLine line);

  default OffsetDateTime map(LocalDateTime value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }
}
