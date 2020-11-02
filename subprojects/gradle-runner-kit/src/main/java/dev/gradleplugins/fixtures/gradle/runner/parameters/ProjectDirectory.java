package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ProjectDirectory extends GradleExecutionParameterImpl<File> implements GradleExecutionCommandLineParameter<File> {
    @Override
    public List<String> getAsArguments() {
        return map(ProjectDirectory::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(File projectDirectory) {
        return Arrays.asList("--project-dir", projectDirectory.getAbsolutePath());
    }

    public static ProjectDirectory unset() {
        return noValue(ProjectDirectory.class);
    }

    public static ProjectDirectory of(File projectDirectory) {
        return fixed(ProjectDirectory.class, projectDirectory);
    }
}
