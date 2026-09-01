package tech.sangdang.lmscoreapi.modules.management.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.helpers.SecurityTestSupport.adminJwt;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.classroom;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.classroomMember;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberPaymentPlanFixtures.AMOUNT;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberPaymentPlanFixtures.CURRENCY;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberPaymentPlanFixtures.PAYMENT_PLAN_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberPaymentPlanFixtures.PREVIOUS_PAYMENT_PLAN_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberPaymentPlanFixtures.currentPaymentPlan;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberPaymentPlanFixtures.paymentPlan;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomMemberPaymentPlanCommand;
import tech.sangdang.lmscoreapi.generated.model.PaymentPlanType;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomMemberPaymentPlanServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMemberPaymentPlanMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberPaymentPlan;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberPaymentPlanRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomMemberPaymentPlanController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomMemberPaymentPlanServiceImpl.class,
  ClassroomMemberPaymentPlanMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom member payment plan management")
class ClassroomMemberPaymentPlanControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomMemberRepository classroomMemberRepository;
  @MockitoBean private ClassroomMemberPaymentPlanRepository classroomMemberPaymentPlanRepository;

  @Test
  @DisplayName("creates a payment plan when the member has none")
  void createClassroomMemberPaymentPlan_noExisting_returns201() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberPaymentPlanRepository.findCurrent(MEMBER_ID)).thenReturn(Optional.empty());
    when(classroomMemberPaymentPlanRepository.insert(any(ClassroomMemberPaymentPlan.class)))
        .thenAnswer(
            invocation -> {
              ClassroomMemberPaymentPlan incoming = invocation.getArgument(0);
              return paymentPlan(
                  PAYMENT_PLAN_ID,
                  incoming.getClassroomMemberId(),
                  incoming.getIsCurrent(),
                  incoming.getReplacedAt(),
                  Instant.now());
            });

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createCommand()))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(PAYMENT_PLAN_ID.toString()))
        .andExpect(jsonPath("$.classroomMemberId").value(MEMBER_ID.toString()))
        .andExpect(jsonPath("$.type").value("PER_SESSION"))
        .andExpect(jsonPath("$.amount").value(AMOUNT))
        .andExpect(jsonPath("$.currency").value(CURRENCY))
        .andExpect(jsonPath("$.isCurrent").value(true));

    verify(classroomMemberPaymentPlanRepository, never())
        .update(any(ClassroomMemberPaymentPlan.class));
    ArgumentCaptor<ClassroomMemberPaymentPlan> captor =
        ArgumentCaptor.forClass(ClassroomMemberPaymentPlan.class);
    verify(classroomMemberPaymentPlanRepository).insert(captor.capture());
    assertThat(captor.getValue().getClassroomMemberId()).isEqualTo(MEMBER_ID);
    assertThat(captor.getValue().getIsCurrent()).isTrue();
    assertThat(captor.getValue().getReplacedAt()).isNull();
    assertThat(captor.getValue().getCurrency()).isEqualTo(CURRENCY);
  }

  @Test
  @DisplayName("defaults omitted currency to VND when creating a payment plan")
  void createClassroomMemberPaymentPlan_omittedCurrency_defaultsToVnd() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberPaymentPlanRepository.findCurrent(MEMBER_ID)).thenReturn(Optional.empty());
    when(classroomMemberPaymentPlanRepository.insert(any(ClassroomMemberPaymentPlan.class)))
        .thenAnswer(
            invocation -> {
              ClassroomMemberPaymentPlan incoming = invocation.getArgument(0);
              return paymentPlan(
                  PAYMENT_PLAN_ID,
                  incoming.getClassroomMemberId(),
                  incoming.getIsCurrent(),
                  incoming.getReplacedAt(),
                  Instant.now());
            });

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        CreateClassroomMemberPaymentPlanCommand.builder()
                            .type(PaymentPlanType.PER_SESSION)
                            .amount(AMOUNT)
                            .build()))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.currency").value(CURRENCY));

    ArgumentCaptor<ClassroomMemberPaymentPlan> captor =
        ArgumentCaptor.forClass(ClassroomMemberPaymentPlan.class);
    verify(classroomMemberPaymentPlanRepository).insert(captor.capture());
    assertThat(captor.getValue().getCurrency()).isEqualTo(CURRENCY);
  }

  @Test
  @DisplayName("replaces the member's current payment plan when creating a new one")
  void createClassroomMemberPaymentPlan_replacesCurrent_returns201() throws Exception {
    ClassroomMemberPaymentPlan existing =
        paymentPlan(
            PREVIOUS_PAYMENT_PLAN_ID, MEMBER_ID, true, null, Instant.now().minusSeconds(60));

    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberPaymentPlanRepository.findCurrent(MEMBER_ID))
        .thenReturn(Optional.of(existing));
    when(classroomMemberPaymentPlanRepository.update(any(ClassroomMemberPaymentPlan.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(classroomMemberPaymentPlanRepository.insert(any(ClassroomMemberPaymentPlan.class)))
        .thenAnswer(
            invocation -> {
              ClassroomMemberPaymentPlan incoming = invocation.getArgument(0);
              return paymentPlan(
                  PAYMENT_PLAN_ID,
                  incoming.getClassroomMemberId(),
                  incoming.getIsCurrent(),
                  incoming.getReplacedAt(),
                  Instant.now());
            });

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createCommand()))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(PAYMENT_PLAN_ID.toString()))
        .andExpect(jsonPath("$.isCurrent").value(true));

    ArgumentCaptor<ClassroomMemberPaymentPlan> updated =
        ArgumentCaptor.forClass(ClassroomMemberPaymentPlan.class);
    verify(classroomMemberPaymentPlanRepository).update(updated.capture());
    assertThat(updated.getValue().getId()).isEqualTo(PREVIOUS_PAYMENT_PLAN_ID);
    assertThat(updated.getValue().getIsCurrent()).isFalse();
    assertThat(updated.getValue().getReplacedAt()).isNotNull();

    ArgumentCaptor<ClassroomMemberPaymentPlan> inserted =
        ArgumentCaptor.forClass(ClassroomMemberPaymentPlan.class);
    verify(classroomMemberPaymentPlanRepository).insert(inserted.capture());
    assertThat(inserted.getValue().getIsCurrent()).isTrue();
    assertThat(inserted.getValue().getReplacedAt()).isNull();

    InOrder inOrder = Mockito.inOrder(classroomMemberPaymentPlanRepository);
    inOrder.verify(classroomMemberPaymentPlanRepository).update(any());
    inOrder.verify(classroomMemberPaymentPlanRepository).insert(any());
  }

  @Test
  @DisplayName("lists an empty payment plan history for a member with none")
  void getClassroomMemberPaymentPlans_none_returns200() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberPaymentPlanRepository.findByMember(MEMBER_ID)).thenReturn(List.of());

    mockMvc
        .perform(
            get(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("lists payment plans with the current plan first")
  void getClassroomMemberPaymentPlans_returns200() throws Exception {
    ClassroomMemberPaymentPlan current = currentPaymentPlan();
    ClassroomMemberPaymentPlan previous =
        paymentPlan(
            PREVIOUS_PAYMENT_PLAN_ID,
            MEMBER_ID,
            false,
            Instant.now(),
            Instant.now().minus(Duration.ofDays(1)));

    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberPaymentPlanRepository.findByMember(MEMBER_ID))
        .thenReturn(List.of(current, previous));

    mockMvc
        .perform(
            get(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(PAYMENT_PLAN_ID.toString()))
        .andExpect(jsonPath("$[0].isCurrent").value(true))
        .andExpect(jsonPath("$[1].id").value(PREVIOUS_PAYMENT_PLAN_ID.toString()))
        .andExpect(jsonPath("$[1].isCurrent").value(false));
  }

  @Test
  @DisplayName("lists current payment plans for a classroom")
  void getAllClassroomPaymentPlans_returns200() throws Exception {
    ClassroomMemberPaymentPlan current = currentPaymentPlan();
    ClassroomMemberPaymentPlan unpaidMemberRow = new ClassroomMemberPaymentPlan();

    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomMemberPaymentPlanRepository.findByClassroom(CLASSROOM_ID))
        .thenReturn(List.of(current, unpaidMemberRow));

    mockMvc
        .perform(
            get("/admin/classrooms/{classroomId}/payment-plans", CLASSROOM_ID).with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(PAYMENT_PLAN_ID.toString()))
        .andExpect(jsonPath("$[0].isCurrent").value(true));
  }

  @Test
  @DisplayName("fails to list classroom payment plans when the classroom does not exist")
  void getAllClassroomPaymentPlans_classroomNotFound_returns404() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get("/admin/classrooms/{classroomId}/payment-plans", CLASSROOM_ID).with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"));

    verify(classroomMemberPaymentPlanRepository, never()).findByClassroom(any());
  }

  @Test
  @DisplayName("deletes a payment plan created within five minutes")
  void deleteClassroomMemberPaymentPlan_withinWindow_returns204() throws Exception {
    ClassroomMemberPaymentPlan plan =
        paymentPlan(PAYMENT_PLAN_ID, MEMBER_ID, true, null, Instant.now().minusSeconds(30));

    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberPaymentPlanRepository.findById(PAYMENT_PLAN_ID))
        .thenReturn(Optional.of(plan));

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans/{paymentPlanId}",
                    CLASSROOM_ID,
                    MEMBER_ID,
                    PAYMENT_PLAN_ID)
                .with(adminJwt()))
        .andExpect(status().isNoContent());

    verify(classroomMemberPaymentPlanRepository).deleteById(PAYMENT_PLAN_ID);
    verify(classroomMemberPaymentPlanRepository, never())
        .update(any(ClassroomMemberPaymentPlan.class));
  }

  @Test
  @DisplayName("rejects deleting a payment plan older than five minutes")
  void deleteClassroomMemberPaymentPlan_outsideWindow_returns409() throws Exception {
    ClassroomMemberPaymentPlan plan =
        paymentPlan(
            PAYMENT_PLAN_ID, MEMBER_ID, true, null, Instant.now().minus(Duration.ofMinutes(6)));

    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberPaymentPlanRepository.findById(PAYMENT_PLAN_ID))
        .thenReturn(Optional.of(plan));

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans/{paymentPlanId}",
                    CLASSROOM_ID,
                    MEMBER_ID,
                    PAYMENT_PLAN_ID)
                .with(adminJwt()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CLASSROOM_MEMBER_PAYMENT_PLAN_DELETE_WINDOW_EXPIRED"));

    verify(classroomMemberPaymentPlanRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("fails to create a payment plan when the member does not exist")
  void createClassroomMemberPaymentPlan_memberMissing_returns404() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            post(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans",
                    CLASSROOM_ID,
                    MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createCommand()))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_MEMBER_NOT_FOUND"));

    verify(classroomMemberPaymentPlanRepository, never()).insert(any());
  }

  @Test
  @DisplayName("fails to delete a payment plan that does not exist")
  void deleteClassroomMemberPaymentPlan_missing_returns404() throws Exception {
    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberPaymentPlanRepository.findById(PAYMENT_PLAN_ID))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans/{paymentPlanId}",
                    CLASSROOM_ID,
                    MEMBER_ID,
                    PAYMENT_PLAN_ID)
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_MEMBER_PAYMENT_PLAN_NOT_FOUND"));

    verify(classroomMemberPaymentPlanRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("fails to delete a payment plan that belongs to another member")
  void deleteClassroomMemberPaymentPlan_wrongMember_returns404() throws Exception {
    UUID otherMemberId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    ClassroomMemberPaymentPlan plan =
        paymentPlan(PAYMENT_PLAN_ID, otherMemberId, true, null, Instant.now());

    when(classroomMemberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(classroomMember()));
    when(classroomMemberPaymentPlanRepository.findById(PAYMENT_PLAN_ID))
        .thenReturn(Optional.of(plan));

    mockMvc
        .perform(
            delete(
                    "/admin/classrooms/{classroomId}/members/{memberId}/payment-plans/{paymentPlanId}",
                    CLASSROOM_ID,
                    MEMBER_ID,
                    PAYMENT_PLAN_ID)
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_MEMBER_PAYMENT_PLAN_NOT_FOUND"));

    verify(classroomMemberPaymentPlanRepository, never()).deleteById(any());
  }

  private static CreateClassroomMemberPaymentPlanCommand createCommand() {
    return CreateClassroomMemberPaymentPlanCommand.builder()
        .type(PaymentPlanType.PER_SESSION)
        .amount(AMOUNT)
        .currency(CURRENCY)
        .build();
  }
}
