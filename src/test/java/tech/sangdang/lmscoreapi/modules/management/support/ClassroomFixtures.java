package tech.sangdang.lmscoreapi.modules.management.support;

import java.time.LocalDateTime;
import java.util.UUID;
import tech.sangdang.lmscoreapi.modules.management.dom.Classroom;

public final class ClassroomFixtures {

  public static final UUID CLASSROOM_ID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
  public static final String CLASSROOM_NAME = "Algebra I";
  public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 7, 14, 0, 0, 0);
  public static final LocalDateTime MODIFIED_AT = LocalDateTime.of(2026, 7, 14, 0, 0, 0);

  private ClassroomFixtures() {}

  public static Classroom classroom() {
    return classroom(CLASSROOM_ID, CLASSROOM_NAME);
  }

  public static Classroom classroom(UUID id, String name) {
    return new Classroom()
        .setId(id)
        .setName(name)
        .setCreatedDate(CREATED_AT)
        .setLastModifiedDate(MODIFIED_AT);
  }
}
