package tech.sangdang.lmscoreapi.modules.management.dom.repository.fragments;

import tech.sangdang.lmscoreapi.modules.management.dom.ClassroomSession;

import java.util.List;

public interface ClassroomSessionRepositoryFragment {
    void insertAllIgnoringDuplicates(List<ClassroomSession> sessions);
}
