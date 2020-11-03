package dev.gradleplugins.runnerkit;

import lombok.EqualsAndHashCode;
import lombok.val;

@EqualsAndHashCode
public final class TaskPath {
    private final String taskPath;

    private TaskPath(String taskPath) {
        this.taskPath = taskPath;
    }

    public String getTaskName() {
        val segments = taskPath.split(":");
        return segments[segments.length - 1];
    }

    public String get() {
        return taskPath;
    }

    public static TaskPath of(String taskPath) {
        return new TaskPath(taskPath);
    }

    @Override
    public String toString() {
        return taskPath;
    }
}
