package tech.sangdang.lmscoreapi.modules.management.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.helpers.SecurityTestSupport.adminJwt;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomInvoiceFixtures.INVOICE_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomInvoiceFixtures.PAID_AMOUNT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomInvoiceFixtures.PAYMENT_NOTE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomInvoiceFixtures.PAYMENT_REFERENCE;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomInvoiceFixtures.approvedInvoice;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomInvoiceFixtures.invoice;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.ACCOUNT_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.classroomMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceLineKind;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoicePaymentMethod;
import tech.sangdang.lmscoreapi.generated.model.CompleteClassroomInvoiceCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomInvoiceAdhocCommand;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomInvoiceAdhocLineCommand;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomInvoiceServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomInvoiceMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoice;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceLine;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceType;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomInvoiceRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomInvoiceController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomInvoiceServiceImpl.class,
  ClassroomInvoiceMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom invoice management")
class ClassroomInvoiceControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomMemberRepository classroomMemberRepository;
  @MockitoBean private ClassroomInvoiceRepository classroomInvoiceRepository;

  @Test
  @DisplayName("creates an adhoc draft invoice and computes line totals")
  void createClassroomInvoiceAdhoc_valid_returns201() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomInvoiceRepository.insert(any(ClassroomInvoice.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/invoices", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(adhocCommand()))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.classroomId").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.memberId").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.type").value("ADHOC"))
        .andExpect(jsonPath("$.totalAmount").value(350_000))
        .andExpect(jsonPath("$.note").value("Makeup fee for missed materials"))
        .andExpect(jsonPath("$.periodStart").doesNotExist())
        .andExpect(jsonPath("$.periodRange").doesNotExist());

    ArgumentCaptor<ClassroomInvoice> captor = ArgumentCaptor.forClass(ClassroomInvoice.class);
    verify(classroomInvoiceRepository).insert(captor.capture());
    ClassroomInvoice saved = captor.getValue();
    assertThat(saved.getStatus()).isEqualTo(ClassroomInvoiceStatus.DRAFT);
    assertThat(saved.getType()).isEqualTo(ClassroomInvoiceType.ADHOC);
    assertThat(saved.getPeriodStart()).isNull();
    assertThat(saved.getPeriodRange()).isNull();
    assertThat(saved.getTotalAmount()).isEqualTo(350_000L);
    assertThat(saved.getLines())
        .extracting(ClassroomInvoiceLine::getKind, ClassroomInvoiceLine::getFinalAmount)
        .containsExactlyInAnyOrder(
            tuple(
                tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceLineKind.LESSON,
                300_000L),
            tuple(
                tech.sangdang.lmscoreapi.modules.management.dom.ClassroomInvoiceLineKind.OTHER,
                50_000L));
  }

  @Test
  @DisplayName("fails to create an adhoc invoice for a member that does not exist")
  void createClassroomInvoiceAdhoc_missingMember_returns404() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/invoices", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(adhocCommand()))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_MEMBER_NOT_FOUND"));

    verify(classroomInvoiceRepository, never()).insert(any());
  }

  @Test
  @DisplayName("fails to create an adhoc invoice for a member in another classroom")
  void createClassroomInvoiceAdhoc_wrongClassroom_returns404() throws Exception {
    UUID otherClassroomId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    when(classroomMemberRepository.findById(MEMBER_ID))
        .thenReturn(
            Optional.of(
                classroomMember(
                    MEMBER_ID, otherClassroomId, ACCOUNT_ID, ClassroomMemberStatus.ACTIVE)));

    mockMvc
        .perform(
            post("/admin/classrooms/{classroomId}/invoices", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(adhocCommand()))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_MEMBER_NOT_FOUND"));

    verify(classroomInvoiceRepository, never()).insert(any());
  }

  @Test
  @DisplayName("completes an approved invoice and records payment")
  void completeClassroomInvoice_approved_returns200() throws Exception {
    when(classroomInvoiceRepository.findById(INVOICE_ID))
        .thenReturn(Optional.of(approvedInvoice()));
    when(classroomInvoiceRepository.update(any(ClassroomInvoice.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/invoices/{invoiceId}/complete",
                    CLASSROOM_ID,
                    INVOICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(completeCommand()))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(INVOICE_ID.toString()))
        .andExpect(jsonPath("$.memberId").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.paidAmount").value(PAID_AMOUNT))
        .andExpect(jsonPath("$.paymentMethod").value("BANK"))
        .andExpect(jsonPath("$.paymentReference").value(PAYMENT_REFERENCE))
        .andExpect(jsonPath("$.paymentNote").value(PAYMENT_NOTE))
        .andExpect(jsonPath("$.paidAt").exists());

    ArgumentCaptor<ClassroomInvoice> captor = ArgumentCaptor.forClass(ClassroomInvoice.class);
    verify(classroomInvoiceRepository).update(captor.capture());
    ClassroomInvoice saved = captor.getValue();
    assertThat(saved.getStatus()).isEqualTo(ClassroomInvoiceStatus.COMPLETED);
    assertThat(saved.getPaidAmount()).isEqualTo(PAID_AMOUNT);
    assertThat(saved.getPaymentMethod().name()).isEqualTo("BANK");
    assertThat(saved.getPaymentReference()).isEqualTo(PAYMENT_REFERENCE);
    assertThat(saved.getPaymentNote()).isEqualTo(PAYMENT_NOTE);
    assertThat(saved.getPaidAt()).isNotNull();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to complete a DRAFT invoice, DRAFT",
    "fails to complete an already COMPLETED invoice, COMPLETED"
  })
  void completeClassroomInvoice_notApproved_returns400(
      String displayName, ClassroomInvoiceStatus status) throws Exception {
    when(classroomInvoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice(status)));

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/invoices/{invoiceId}/complete",
                    CLASSROOM_ID,
                    INVOICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(completeCommand()))
                .with(adminJwt()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CLASSROOM_INVOICE_NOT_APPROVED"));

    verify(classroomInvoiceRepository, never()).update(any());
  }

  @Test
  @DisplayName("fails to complete an invoice that does not exist")
  void completeClassroomInvoice_missing_returns404() throws Exception {
    when(classroomInvoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/invoices/{invoiceId}/complete",
                    CLASSROOM_ID,
                    INVOICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(completeCommand()))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_INVOICE_NOT_FOUND"));

    verify(classroomInvoiceRepository, never()).update(any());
  }

  @Test
  @DisplayName("fails to complete an invoice that belongs to another classroom")
  void completeClassroomInvoice_wrongClassroom_returns404() throws Exception {
    UUID otherClassroomId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    when(classroomInvoiceRepository.findById(INVOICE_ID))
        .thenReturn(Optional.of(approvedInvoice().setClassroomId(otherClassroomId)));

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/invoices/{invoiceId}/complete",
                    CLASSROOM_ID,
                    INVOICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(completeCommand()))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_INVOICE_NOT_FOUND"));

    verify(classroomInvoiceRepository, never()).update(any());
  }

  private static CreateClassroomInvoiceAdhocCommand adhocCommand() {
    return CreateClassroomInvoiceAdhocCommand.builder()
        .memberId(MEMBER_ID)
        .note("Makeup fee for missed materials")
        .lines(
            List.of(
                CreateClassroomInvoiceAdhocLineCommand.builder()
                    .kind(ClassroomInvoiceLineKind.LESSON)
                    .description("Two extra lessons")
                    .unitPrice(150_000L)
                    .quantity(2)
                    .build(),
                CreateClassroomInvoiceAdhocLineCommand.builder()
                    .kind(ClassroomInvoiceLineKind.OTHER)
                    .description("Replacement workbook")
                    .unitPrice(50_000L)
                    .quantity(1)
                    .build()))
        .build();
  }

  private static CompleteClassroomInvoiceCommand completeCommand() {
    return CompleteClassroomInvoiceCommand.builder()
        .paidAmount(PAID_AMOUNT)
        .paymentMethod(ClassroomInvoicePaymentMethod.BANK)
        .paymentReference(PAYMENT_REFERENCE)
        .paymentNote(PAYMENT_NOTE)
        .build();
  }
}
