package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.File;

public interface WorkingDirectoryParameter extends DirectoryParameter {
    File orElse(File other);

    static WorkingDirectoryParameter unset() {
        return new UnsetWorkingDirectoryParameter();
    }

    static WorkingDirectoryParameter of(File workingDirectory) {
        return new DefaultWorkingDirectoryParameter(workingDirectory);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class UnsetWorkingDirectoryParameter extends UnsetParameter<File> implements WorkingDirectoryParameter {
    }

    @Value
    class DefaultWorkingDirectoryParameter implements WorkingDirectoryParameter {
        File value;

        @Override
        public File orElse(File other) {
            return value;
        }

        @Override
        public File getAsFile() {
            return value;
        }
    }
}
