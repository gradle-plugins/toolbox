package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;

import java.util.Set;

// TODO: Use Nokee's implementation
public interface TaskView<T extends Task> {
    void configureEach(Action<? super T> action);

    Provider<Set<T>> getElements();
}
