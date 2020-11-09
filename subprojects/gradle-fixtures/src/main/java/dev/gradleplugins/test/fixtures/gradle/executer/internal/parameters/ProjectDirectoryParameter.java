package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Deprecated
public final class ProjectDirectoryParameter extends GradleExecutionParameterImpl<ProjectDirectory> implements CommandLineGradleExecutionParameter<ProjectDirectory>, DirectoryParameter<ProjectDirectory> {
    @Override
    public List<String> getAsArguments() {
        if (isPresent()) {
            return Arrays.asList("--project-dir", get().getAbsolutePath());
        }
        return Collections.emptyList();
    }

    public static ProjectDirectoryParameter unset() {
        return noValue(ProjectDirectoryParameter.class);
    }

    public static ProjectDirectoryParameter of(ProjectDirectory projectDirectory) {
        return fixed(ProjectDirectoryParameter.class, projectDirectory);
    }
}
