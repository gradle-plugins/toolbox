package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import org.gradle.api.logging.configuration.ShowStacktrace;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface StacktraceParameter extends CommandLineGradleParameter {
    static StacktraceParameter hide() {
        return new HideStacktraceParameter();
    }

    static StacktraceParameter show() {
        return new ShowStacktraceParameter();
    }

    class HideStacktraceParameter implements StacktraceParameter {
        @Override
        public List<String> getAsArguments() {
            return Collections.emptyList();
        }
    }

    class ShowStacktraceParameter implements StacktraceParameter {
        @Override
        public List<String> getAsArguments() {
            return Arrays.asList("--stacktrace");
        }
    }
}
