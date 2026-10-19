package tech.sangdang.lmscoreapi.modules.management.app.useCase.InvoiceGeneration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.classroom;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_NAME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.SECOND_MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.classroomMember;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberPaymentPlanFixtures.PAYMENT_PLAN_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberPaymentPlanFixtures.paymentPlan;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.SESSION_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomSessionFixtures.classroomSessionAttendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoice;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceLine;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceLineKind;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoicePeriodRange;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceType;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMember;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberPaymentPlan;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendance;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSessionAttendanceStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberPaymentPlanRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomSessionAttendanceRepository;
import tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("Classroom invoice generation")
class ClassroomInvoiceGenerationServiceImplTest {

  private static final LocalDateTime WINDOW_START = LocalDateTime.of(2026, 9, 1, 0, 0);
  private static final LocalDateTime WINDOW_END = LocalDateTime.of(2026, 10, 1, 0, 0);

  @Mock private ClassroomRepository classroomRepository;
  @Mock private ClassroomMemberRepository classroomMemberRepository;
  @Mock private ClassroomSessionAttendanceRepository classroomSessionAttendanceRepository;
  @Mock private ClassroomMemberPaymentPlanRepository classroomMemberPaymentPlanRepository;
  @Mock private ClassroomInvoiceGenerationRepository classroomInvoiceGenerationRepository;

  private ClassroomInvoiceGenerationServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new ClassroomInvoiceGenerationServiceImpl(
            classroomRepository,
            classroomMemberRepository,
            classroomSessionAttendanceRepository,
            classroomMemberPaymentPlanRepository,
            classroomInvoiceGenerationRepository);
  }

  @Test
  @DisplayName("skips insert when there are no active classrooms")
  void generateInvoices_noClassrooms_doesNotInsert() {
    when(classroomRepository.query(any(BaseQuery.class))).thenReturn(Stream.empty());

    service.generateInvoices(WINDOW_START, WINDOW_END);

    verify(classroomMemberRepository, never()).query(any(BaseQuery.class));
    verify(classroomInvoiceGenerationRepository, never()).insertAllIgnoringDuplicates(anyList());
  }

  @Test
  @DisplayName("skips insert when there are no active students")
  void generateInvoices_noStudents_doesNotInsert() {
    when(classroomRepository.query(any(BaseQuery.class))).thenReturn(Stream.of(classroom()));
    when(classroomMemberRepository.query(any(BaseQuery.class))).thenReturn(Stream.empty());

    service.generateInvoices(WINDOW_START, WINDOW_END);

    verify(classroomSessionAttendanceRepository, never()).query(any(BaseQuery.class));
    verify(classroomInvoiceGenerationRepository, never()).insertAllIgnoringDuplicates(anyList());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pricedInvoiceCases")
  @DisplayName("builds a GENERATED draft invoice from attendances and the current plan")
  void generateInvoices_studentWithAttendancesAndPlan_insertsPricedInvoice(
      String unusedName, int attendanceCount, Long planAmount) {
    long expectedUnitPrice = planAmount == null ? 0L : planAmount;
    long expectedTotal = expectedUnitPrice * attendanceCount;
    List<ClassroomSessionAttendance> attendances = attendances(attendanceCount);
    List<ClassroomMemberPaymentPlan> plans =
        planAmount == null ? List.of() : List.of(currentPlan(planAmount));

    when(classroomRepository.query(any(BaseQuery.class))).thenReturn(Stream.of(classroom()));
    when(classroomMemberRepository.query(any(BaseQuery.class)))
        .thenReturn(Stream.of(classroomMember()));
    when(classroomSessionAttendanceRepository.query(any(BaseQuery.class)))
        .thenReturn(attendances.stream());
    when(classroomMemberPaymentPlanRepository.findCurrentByMembers(List.of(MEMBER_ID)))
        .thenReturn(plans);

    service.generateInvoices(WINDOW_START, WINDOW_END);

    ClassroomInvoice invoice = only(capturedInserts());
    assertThat(invoice.getClassroomId()).isEqualTo(CLASSROOM_ID);
    assertThat(invoice.getMemberId()).isEqualTo(MEMBER_ID);
    assertThat(invoice.getStatus()).isEqualTo(ClassroomInvoiceStatus.DRAFT);
    assertThat(invoice.getType()).isEqualTo(ClassroomInvoiceType.GENERATED);
    assertThat(invoice.getPeriodStart()).isEqualTo(LocalDate.of(2026, 9, 1));
    assertThat(invoice.getPeriodRange()).isEqualTo(ClassroomInvoicePeriodRange.MONTH);
    assertThat(invoice.getTotalAmount()).isEqualTo(expectedTotal);
    assertThat(invoice.getNote()).contains(MEMBER_NAME).contains("SEPTEMBER");

    ClassroomInvoiceLine line = only(invoice.getLines());
    assertThat(line.getKind()).isEqualTo(ClassroomInvoiceLineKind.LESSON);
    assertThat(line.getUnitPrice()).isEqualTo(expectedUnitPrice);
    assertThat(line.getQuantity()).isEqualTo(attendanceCount);
    assertThat(line.getFinalAmount()).isEqualTo(expectedTotal);
    if (attendanceCount == 0) {
      assertThat(line.getDescription()).isEqualTo("Attended dates: N/A");
    } else {
      assertThat(line.getDescription()).startsWith("Attended dates: ");
      attendances.forEach(
          attendance ->
              assertThat(line.getDescription())
                  .contains(attendance.getAttendanceDate().toString()));
    }
  }

  static Stream<Arguments> pricedInvoiceCases() {
    return Stream.of(
        Arguments.of("0 attendances × 150_000 plan → total 0", 0, 150_000L),
        Arguments.of("1 attendance × 150_000 plan → total 150_000", 1, 150_000L),
        Arguments.of("2 attendances × 150_000 plan → total 300_000", 2, 150_000L),
        Arguments.of("3 attendances × 200_000 plan → total 600_000", 3, 200_000L),
        Arguments.of("4 attendances × 50_000 plan → total 200_000", 4, 50_000L),
        Arguments.of("2 attendances × 0 plan → total 0", 2, 0L),
        Arguments.of("1 attendance × no plan → unit price 0", 1, null),
        Arguments.of("0 attendances × no plan → total 0", 0, null));
  }

  @Test
  @DisplayName("inserts one invoice per active student")
  void generateInvoices_twoStudents_insertsTwoInvoices() {
    ClassroomMember second =
        classroomMember(
            SECOND_MEMBER_ID,
            CLASSROOM_ID,
            ClassroomMemberFixtures.SECOND_ACCOUNT_ID,
            ClassroomMemberStatus.ACTIVE);

    when(classroomRepository.query(any(BaseQuery.class))).thenReturn(Stream.of(classroom()));
    when(classroomMemberRepository.query(any(BaseQuery.class)))
        .thenReturn(Stream.of(classroomMember(), second));
    when(classroomSessionAttendanceRepository.query(any(BaseQuery.class)))
        .thenReturn(Stream.empty());
    when(classroomMemberPaymentPlanRepository.findCurrentByMembers(
            List.of(MEMBER_ID, SECOND_MEMBER_ID)))
        .thenReturn(List.of());

    service.generateInvoices(WINDOW_START, WINDOW_END);

    assertThat(capturedInserts())
        .extracting(ClassroomInvoice::getMemberId)
        .containsExactlyInAnyOrder(MEMBER_ID, SECOND_MEMBER_ID);
  }

  private static ClassroomMemberPaymentPlan currentPlan(long amount) {
    LocalDateTime now = LocalDateTime.now();
    return paymentPlan(PAYMENT_PLAN_ID, MEMBER_ID, now, null, now).setAmount(amount);
  }

  private static List<ClassroomSessionAttendance> attendances(int count) {
    return IntStream.range(0, count)
        .mapToObj(
            i ->
                classroomSessionAttendance(
                        UUID.nameUUIDFromBytes(("attendance-" + i).getBytes()),
                        SESSION_ID,
                        MEMBER_ID,
                        ClassroomSessionAttendanceStatus.ATTENDED)
                    .setAttendanceDate(LocalDateTime.of(2026, 9, 1 + i * 7, 9, 0)))
        .toList();
  }

  @SuppressWarnings("unchecked")
  private List<ClassroomInvoice> capturedInserts() {
    ArgumentCaptor<List<ClassroomInvoice>> captor = ArgumentCaptor.forClass(List.class);
    verify(classroomInvoiceGenerationRepository).insertAllIgnoringDuplicates(captor.capture());
    return captor.getValue();
  }

  private static <T> T only(List<T> items) {
    assertThat(items).hasSize(1);
    return items.getFirst();
  }

  private static <T> T only(Set<T> items) {
    assertThat(items).hasSize(1);
    return items.iterator().next();
  }
}
