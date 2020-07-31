package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

public final class WorkingDirectoryParameter extends GradleExecutionParameterImpl<WorkingDirectory> implements DirectoryParameter<WorkingDirectory> {
    public static WorkingDirectoryParameter unset() {
        return noValue(WorkingDirectoryParameter.class);
    }

    public static WorkingDirectoryParameter of(WorkingDirectory workingDirectory) {
        return fixed(WorkingDirectoryParameter.class, workingDirectory);
    }
}
