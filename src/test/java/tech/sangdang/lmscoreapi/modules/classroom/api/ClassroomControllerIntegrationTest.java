package tech.sangdang.lmscoreapi.modules.classroom.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.sangdang.lmscoreapi.modules.classroom.support.ClassroomFixtures.CLASSROOM_ID;
import static tech.sangdang.lmscoreapi.modules.classroom.support.ClassroomFixtures.CLASSROOM_NAME;
import static tech.sangdang.lmscoreapi.modules.classroom.support.ClassroomFixtures.classroom;

import java.util.Optional;
import java.util.stream.Stream;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tech.sangdang.lmscoreapi.common.exception.GlobalExceptionHandler;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.generated.model.ClassroomFilter;
import tech.sangdang.lmscoreapi.generated.model.CreateClassroomCommand;
import tech.sangdang.lmscoreapi.generated.model.UpdateClassroomCommand;
import tech.sangdang.lmscoreapi.modules.classroom.app.impl.ClassroomManagementServiceImpl;
import tech.sangdang.lmscoreapi.modules.classroom.app.mappers.ClassroomMapperImpl;
import tech.sangdang.lmscoreapi.modules.classroom.dom.Classroom;
import tech.sangdang.lmscoreapi.modules.classroom.dom.repository.ClassroomRepository;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ClassroomController.class)
@Import({
  GlobalExceptionHandler.class,
  ClassroomManagementServiceImpl.class,
  ClassroomMapperImpl.class,
})
@DisplayName("Classroom management")
class ClassroomControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private ClassroomRepository classroomRepository;

  @Test
  @DisplayName("creates a classroom")
  void createClassroom_valid_returns201() throws Exception {
    when(classroomRepository.insert(any(Classroom.class)))
        .thenAnswer(
            invocation -> {
              Classroom incoming = invocation.getArgument(0);
              return classroom(CLASSROOM_ID, incoming.getName());
            });

    CreateClassroomCommand command = CreateClassroomCommand.builder().name(CLASSROOM_NAME).build();

    mockMvc
        .perform(
            post("/classrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.name").value(CLASSROOM_NAME))
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());

    ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
    verify(classroomRepository).insert(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo(CLASSROOM_NAME);
  }

  @Test
  @DisplayName("gets a classroom by id")
  void getClassroomById_found_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));

    mockMvc
        .perform(get("/classrooms/{id}", CLASSROOM_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.name").value(CLASSROOM_NAME))
        .andExpect(jsonPath("$.createdDate").exists())
        .andExpect(jsonPath("$.lastModifiedDate").exists());
  }

  @Test
  @DisplayName("updates a classroom name")
  void updateClassroom_valid_returns200() throws Exception {
    when(classroomRepository.findById(CLASSROOM_ID)).thenReturn(Optional.of(classroom()));
    when(classroomRepository.update(any(Classroom.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateClassroomCommand command = UpdateClassroomCommand.builder().name("Algebra II").build();

    mockMvc
        .perform(
            put("/classrooms/{id}", CLASSROOM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(command)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$.name").value("Algebra II"));

    ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
    verify(classroomRepository).update(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo("Algebra II");
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
          case "GET" -> get("/classrooms/{id}", CLASSROOM_ID);
          case "PUT" ->
              put("/classrooms/{id}", CLASSROOM_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      jsonMapper.writeValueAsString(
                          UpdateClassroomCommand.builder().name("Algebra II").build()));
          default -> throw new IllegalArgumentException("Unsupported method: " + httpMethod);
        };

    mockMvc
        .perform(request)
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
            post("/classrooms/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(filter)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(CLASSROOM_ID.toString()))
        .andExpect(jsonPath("$[0].name").value(CLASSROOM_NAME));

    verify(classroomRepository).query(any(BaseQuery.class));
  }
}
