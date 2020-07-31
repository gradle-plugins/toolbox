package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class GradleUserHomeDirectoryParameter extends GradleExecutionParameterImpl<GradleUserHomeDirectory> implements CommandLineGradleExecutionParameter<GradleUserHomeDirectory>, DirectoryParameter<GradleUserHomeDirectory> {
    public static GradleUserHomeDirectoryParameter unset() {
        return noValue(GradleUserHomeDirectoryParameter.class);
    }

    public static GradleUserHomeDirectoryParameter of(GradleUserHomeDirectory gradleUserHomeDirectory) {
        return fixed(GradleUserHomeDirectoryParameter.class, gradleUserHomeDirectory);
    }

    @Override
    public List<String> getAsArguments() {
        if (isPresent()) {
            return Arrays.asList("--gradle-user-home", get().getAbsolutePath());
        }
        return Collections.emptyList();
    }
}
