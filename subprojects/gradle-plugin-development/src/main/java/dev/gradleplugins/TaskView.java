package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.Task;

// TODO: Use Nokee's implementation
public interface TaskView<T extends Task> {
    void configureEach(Action<? super T> action);
}
