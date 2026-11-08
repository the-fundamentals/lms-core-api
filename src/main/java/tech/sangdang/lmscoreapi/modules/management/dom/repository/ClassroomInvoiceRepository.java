package tech.sangdang.lmscoreapi.modules.management.dom.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sangdang.lmscoreapi.common.persistence.BaseCommandRepository;
import tech.sangdang.lmscoreapi.common.persistence.BaseQueryRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoice;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoicePeriodRange;

@Repository
public interface ClassroomInvoiceRepository
    extends BaseCommandRepository<ClassroomInvoice, UUID>,
        BaseQueryRepository<ClassroomInvoice, UUID> {

  /**
   * Finds the GENERATED invoice for the member/period, if any.
   *
   * @param memberId member billed
   * @param periodRange period range
   * @param periodStart period start
   */
  @Query(
      """
      SELECT * FROM classroom_invoice
      WHERE member_id = :memberId
        AND type = 'GENERATED'
        AND period_range = :periodRange
        AND period_start = :periodStart
      """)
  Optional<ClassroomInvoice> findGeneratedByMemberAndPeriod(
      @NonNull @Param("memberId") UUID memberId,
      @NonNull @Param("periodRange") ClassroomInvoicePeriodRange periodRange,
      @NonNull @Param("periodStart") LocalDate periodStart);
}
