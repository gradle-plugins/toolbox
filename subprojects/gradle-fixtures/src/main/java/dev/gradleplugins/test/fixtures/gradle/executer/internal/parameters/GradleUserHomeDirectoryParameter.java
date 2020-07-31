package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public interface GradleUserHomeDirectoryParameter extends CommandLineGradleParameter, DirectoryParameter {
    static GradleUserHomeDirectoryParameter unset() {
        return new UnsetGradleUserHomeDirectoryParameter();
    }

    static GradleUserHomeDirectoryParameter of(File gradleUserHomeDirectory) {
        return new DefaultGradleUserHomeDirectoryParameter(gradleUserHomeDirectory);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class UnsetGradleUserHomeDirectoryParameter extends UnsetParameter<File> implements GradleUserHomeDirectoryParameter {}

    @Value
    class DefaultGradleUserHomeDirectoryParameter implements GradleUserHomeDirectoryParameter {
        File value;

        @Override
        public File getAsFile() {
            return value;
        }

        @Override
        public List<String> getAsArguments() {
            return Arrays.asList("--gradle-user-home", value.getAbsolutePath());
        }
    }
}
