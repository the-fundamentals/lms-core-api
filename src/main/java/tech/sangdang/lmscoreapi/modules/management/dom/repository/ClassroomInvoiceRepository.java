package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoice;

@Repository
public interface ClassroomInvoiceRepository
    extends BaseCommandRepository<ClassroomInvoice, UUID>,
        BaseQueryRepository<ClassroomInvoice, UUID> {
}
