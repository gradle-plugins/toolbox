package dev.gradleplugins.fixtures.gradle.runner;

import lombok.EqualsAndHashCode;
import lombok.val;

@EqualsAndHashCode
public final class GradleTaskPath {
    private final String taskPath;

    private GradleTaskPath(String taskPath) {
        this.taskPath = taskPath;
    }

    public String getTaskName() {
        val segments = taskPath.split(":");
        return segments[segments.length - 1];
    }

    public String get() {
        return taskPath;
    }

    public static GradleTaskPath of(String taskPath) {
        return new GradleTaskPath(taskPath);
    }

    @Override
    public String toString() {
        return taskPath;
    }
}
