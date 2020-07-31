package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface DeprecationChecksParameter extends CommandLineGradleParameter {
    static DeprecationChecksParameter fails() {
        return new FailingDeprecationChecksParameter();
    }

    static DeprecationChecksParameter ignores() {
        return new IgnoringDeprecationChecksParameter();
    }

    class FailingDeprecationChecksParameter implements DeprecationChecksParameter {
        @Override
        public List<String> getAsArguments() {
            return Arrays.asList("--warning-mode", "fail");
        }
    }

    class IgnoringDeprecationChecksParameter implements DeprecationChecksParameter {
        @Override
        public List<String> getAsArguments() {
            return Collections.emptyList();
        }
    }
}
