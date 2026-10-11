package tech.sangdang.lmscoreapi.modules.management.app.useCase.SessionGeneration;

import java.util.List;
import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;

public interface ClassroomSessionRepositoryFragment {

  /** Inserts sessions; skips existing rows for the same classroom, recurrence, and date. */
  void insertAllIgnoringDuplicates(List<ClassroomSession> sessions);
}
