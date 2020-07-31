package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public interface ProjectDirectoryParameter extends CommandLineGradleParameter, DirectoryParameter {
    List<String> getAsArguments();

    static ProjectDirectoryParameter unset() {
        return new UnsetProjectDirectoryParameter();
    }

    static ProjectDirectoryParameter of(File projectDirectory) {
        return new DefaultProjectDirectoryParameter(projectDirectory);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class UnsetProjectDirectoryParameter extends UnsetParameter<File> implements ProjectDirectoryParameter {}

    @Value
    class DefaultProjectDirectoryParameter implements ProjectDirectoryParameter {
        File value;

        @Override
        public List<String> getAsArguments() {
            return Arrays.asList("--project-dir", value.getAbsolutePath());
        }

        @Override
        public File getAsFile() {
            return value;
        }
    }
}
