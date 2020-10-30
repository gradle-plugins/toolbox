package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.io.File;

public final class WorkingDirectory extends GradleExecutionParameterImpl<File> implements GradleExecutionParameter<File> {
    public static WorkingDirectory unset() {
        return noValue(WorkingDirectory.class);
    }

    public static WorkingDirectory of(File workingDirectory) {
        return fixed(WorkingDirectory.class, workingDirectory);
    }
}
