package tech.sangdang.lmscoreapi.modules.management.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.helpers.SecurityTestSupport.adminJwt;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.BANNER_KEY;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.CLASSROOM_NAME;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.NEW_BANNER_KEY;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomFixtures.classroom;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberFixtures.MEMBER_ID;
import static tech.sangdang.lmscoreapi.modules.management.support.ClassroomMemberPaymentPlanFixtures.PAYMENT_PLAN_ID;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.config.SecurityConfig;
import tech.sangdang.lmscoreapi.generated.model.ClassroomBasePaymentPlanInput;
import tech.sangdang.lmscoreapi.generated.model.ClassroomFilter;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomCommand;
import tech.sangdang.lmscoreapi.generated.model.PaymentPlanType;
import tech.sangdang.lmscoreapi.generated.model.SetClassroomBasePaymentPlanCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomCommand;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomManagementServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomMemberPaymentPlanServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.internal.ClassroomRecordService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMemberPaymentPlanMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomMemberPaymentPlan;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomStatus;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberPaymentPlanRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.utility.app.StorageService;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomManagementServiceImpl.class,
  ClassroomMemberPaymentPlanServiceImpl.class,
  ClassroomRecordService.class,
  ClassroomMapperImpl.class,
  ClassroomMemberPaymentPlanMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom management")
class ClassroomControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
  @MockitoBean private ClassroomMemberRepository classroomMemberRepository;
  @MockitoBean private ClassroomMemberPaymentPlanRepository classroomMemberPaymentPlanRepository;
  @MockitoBean private StorageService storageService;

  @Test
  @DisplayName("creates a classroom")
  void createClassroom_valid_returns201() throws Exception {
    when(classroomRepository.insert(any(Classroom.class)))
        .thenAnswer(
            invocation -> {
              Classroom incoming = invocation.getArgument(0);
              return classroom(CLASSROOM_ID, incoming.getName())
                  .setBannerKey(incoming.getBannerKey())
                  .setNumberOfMembers(incoming.getNumberOfMembers())
                  .setStatus(incoming.getStatus())
                  .setBasePaymentPlanType(incoming.getBasePaymentPlanType())
                  .setBasePaymentPlanAmount(incoming.getBasePaymentPlanAmount());
            });

    CreateClassroomCommand command =
        CreateClassroomCommand.builder().name(CLASSROOM_NAME).bannerKey(BANNER_KEY).build();

    mockMvc
        .perform(
            post("/admin/classrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.name").value(CLASSROOM_NAME))
        .andExpect(jsonPath("$.bannerKey").value(BANNER_KEY))
        .andExpect(jsonPath("$.numberOfMembers").value(0))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.basePaymentPlanType").value("PER_SESSION"))
        .andExpect(jsonPath("$.basePaymentPlanAmount").value(0))
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());

    ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
    InOrder order = inOrder(classroomRepository, storageService);
    order.verify(classroomRepository).insert(captor.capture());
    order
        .verify(storageService)
        .confirmPublicFileUpload(new ConfirmUploadPublicCommand(BANNER_KEY));
    assertThat(captor.getValue().getName()).isEqualTo(CLASSROOM_NAME);
    assertThat(captor.getValue().getBannerKey()).isEqualTo(BANNER_KEY);
    assertThat(captor.getValue().getNumberOfMembers()).isZero();
    assertThat(captor.getValue().getStatus()).isEqualTo(ClassroomStatus.ACTIVE);
    assertThat(captor.getValue().getBasePaymentPlanAmount()).isZero();
    assertThat(captor.getValue().getBasePaymentPlanType())
        .isEqualTo(tech.sangdang.lmscoreapi.modules.management.dom.PaymentPlanType.PER_SESSION);
  }

  @Test
  @DisplayName("creates a classroom with the given base payment plan")
  void createClassroom_withBasePaymentPlan_returns201() throws Exception {
    when(classroomRepository.insert(any(Classroom.class)))
        .thenAnswer(
            invocation -> {
              Classroom incoming = invocation.getArgument(0);
              return classroom(CLASSROOM_ID, incoming.getName())
                  .setBasePaymentPlanType(incoming.getBasePaymentPlanType())
                  .setBasePaymentPlanAmount(incoming.getBasePaymentPlanAmount());
            });

    CreateClassroomCommand command =
        CreateClassroomCommand.builder()
            .name(CLASSROOM_NAME)
            .basePaymentPlan(
                ClassroomBasePaymentPlanInput.builder().amount(150_000L).build())
            .build();

    mockMvc
        .perform(
            post("/admin/classrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.basePaymentPlanAmount").value(150_000))
        .andExpect(jsonPath("$.basePaymentPlanType").value("PER_SESSION"));

    verify(storageService, never()).confirmPublicFileUpload(any());
  }

  @Test
  @DisplayName("replaces active students still on the classroom base when the base changes")
  void setClassroomBasePaymentPlan_changed_replacesBasePlans() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomRepository.update(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    ClassroomMemberPaymentPlan current =
        new ClassroomMemberPaymentPlan()
            .setId(PAYMENT_PLAN_ID)
            .setClassroomMemberId(MEMBER_ID)
            .setType(tech.sangdang.lmscoreapi.modules.management.dom.PaymentPlanType.PER_SESSION)
            .setAmount(0L)
            .setFromClassroomBase(true);
    when(classroomMemberPaymentPlanRepository.findCurrentBasePlansForActiveStudents(CLASSROOM_ID))
        .thenReturn(List.of(current));
    when(classroomMemberPaymentPlanRepository.findCurrent(MEMBER_ID))
        .thenReturn(Optional.of(current));
    when(classroomMemberPaymentPlanRepository.update(any(ClassroomMemberPaymentPlan.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(classroomMemberPaymentPlanRepository.insert(any(ClassroomMemberPaymentPlan.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            put("/admin/classrooms/{classroomId}/base-payment-plan", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        SetClassroomBasePaymentPlanCommand.builder()
                            .amount(200_000L)
                            .type(PaymentPlanType.PER_SESSION)
                            .build()))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.basePaymentPlanAmount").value(200_000));

    assertThat(current.getEffectiveTo()).isNotNull();
    ArgumentCaptor<ClassroomMemberPaymentPlan> inserted =
        ArgumentCaptor.forClass(ClassroomMemberPaymentPlan.class);
    verify(classroomMemberPaymentPlanRepository).insert(inserted.capture());
    assertThat(inserted.getValue().getAmount()).isEqualTo(200_000L);
    assertThat(inserted.getValue().getClassroomMemberId()).isEqualTo(MEMBER_ID);
    assertThat(inserted.getValue().isFromClassroomBase()).isTrue();
    assertThat(inserted.getValue().getEffectiveTo()).isNull();
  }

  @Test
  @DisplayName("does not rewrite member plans when the classroom base is unchanged")
  void setClassroomBasePaymentPlan_unchanged_doesNotRewrite() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomRepository.update(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            put("/admin/classrooms/{classroomId}/base-payment-plan", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        SetClassroomBasePaymentPlanCommand.builder().amount(0L).build()))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.basePaymentPlanAmount").value(0));

    verify(classroomMemberPaymentPlanRepository, never())
        .findCurrentBasePlansForActiveStudents(any());
    verify(classroomMemberPaymentPlanRepository, never()).insert(any());
  }

  @Test
  @DisplayName("fails to set a base payment plan when the classroom does not exist")
  void setClassroomBasePaymentPlan_missing_returns404() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            put("/admin/classrooms/{classroomId}/base-payment-plan", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    jsonMapper.writeValueAsString(
                        SetClassroomBasePaymentPlanCommand.builder().amount(1L).build()))
                .with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"));

    verify(classroomRepository, never()).update(any());
  }

  @Test
  @DisplayName("gets a classroom by id")
  void getClassroomById_found_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));

    mockMvc
        .perform(get("/admin/classrooms/{id}", CLASSROOM_ID).with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.name").value(CLASSROOM_NAME))
        .andExpect(jsonPath("$.bannerKey").value(BANNER_KEY))
        .andExpect(jsonPath("$.numberOfMembers").value(0))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());
  }

  @Test
  @DisplayName("updates a classroom name and banner")
  void updateClassroom_valid_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomRepository.update(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateClassroomCommand command =
        UpdateClassroomCommand.builder().name("Algebra II").bannerKey(NEW_BANNER_KEY).build();

    mockMvc
        .perform(
            put("/admin/classrooms/{id}", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.name").value("Algebra II"))
        .andExpect(jsonPath("$.bannerKey").value(NEW_BANNER_KEY))
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
    InOrder order = inOrder(classroomRepository, storageService);
    order.verify(classroomRepository).update(captor.capture());
    order
        .verify(storageService)
        .confirmPublicFileUpload(new ConfirmUploadPublicCommand(NEW_BANNER_KEY));
    assertThat(captor.getValue().getName()).isEqualTo("Algebra II");
    assertThat(captor.getValue().getBannerKey()).isEqualTo(NEW_BANNER_KEY);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fails to get a classroom that does not exist, GET",
    "fails to update a classroom that does not exist, PUT"
  })
  void classroomLookup_failsWhenMissing(String displayName, String httpMethod) throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.empty());

    MockHttpServletRequestBuilder request =
        switch (httpMethod) {
          case "GET" -> get("/admin/classrooms/{id}", CLASSROOM_ID);
          case "PUT" ->
              put("/admin/classrooms/{id}", CLASSROOM_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      jsonMapper.writeValueAsString(
                          UpdateClassroomCommand.builder().name("Algebra II").build()));
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request.with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CLASSROOM_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));

    verify(classroomRepository, never()).update(any());
  }

  @Test
  @DisplayName("queries classrooms")
  void getAllClassrooms_returns200() throws Exception {
    when(classroomRepository.query(any(BaseQuery.class))).thenReturn(Stream.of(classroom()));

    ClassroomFilter filter = ClassroomFilter.builder().build();

    mockMvc
        .perform(
            post("/admin/classrooms/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter))
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$[0].name").value(CLASSROOM_NAME))
        .andExpect(jsonPath("$[0].bannerKey").value(BANNER_KEY))
        .andExpect(jsonPath("$[0].numberOfMembers").value(0))
        .andExpect(jsonPath("$[0].status").value("ACTIVE"));

    verify(classroomRepository).query(any(BaseQuery.class));
  }
}
