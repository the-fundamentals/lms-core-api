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
import tech.sangdang.lmscoreapi.generated.model.ClassroomFilter;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomCommand;
import tech.sangdang.lmscoreapi.modules.management.app.impl.ClassroomManagementServiceImpl;
import tech.sangdang.lmscoreapi.modules.management.app.internal.ClassroomRecordService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomMapperImpl;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;
import tech.sangdang.lmscoreapi.modules.utility.app.StorageService;
import tech.sangdang.lmscoreapi.modules.utility.app.dto.ConfirmUploadPublicCommand;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomManagementServiceImpl.class,
  ClassroomRecordService.class,
  ClassroomMapperImpl.class,
  SecurityConfig.class,
})
@DisplayName("Classroom management")
class ClassroomControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;
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
                  .setNumberOfMembers(incoming.getNumberOfMembers());
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
        .andExpect(jsonPath("$.bannerKey").value(NEW_BANNER_KEY));

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
        .andExpect(jsonPath("$[0].numberOfMembers").value(0));

    verify(classroomRepository).query(any(BaseQuery.class));
  }
}
