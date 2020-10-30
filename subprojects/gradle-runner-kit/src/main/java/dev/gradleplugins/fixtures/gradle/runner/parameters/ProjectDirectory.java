package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;

public final class ProjectDirectory extends GradleExecutionParameterImpl<File> implements GradleExecutionCommandLineParameter<File> {
    @Override
    public List<String> getAsArguments() {
        return map(ProjectDirectory::asArguments).orElseGet(ImmutableList::of);
    }

    private static List<String> asArguments(File projectDirectory) {
        return ImmutableList.of("--project-dir", projectDirectory.getAbsolutePath());
    }

    public static ProjectDirectory unset() {
        return noValue(ProjectDirectory.class);
    }

    public static ProjectDirectory of(File projectDirectory) {
        return fixed(ProjectDirectory.class, projectDirectory);
    }
}
