package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Deprecated
public final class StacktraceParameter extends GradleExecutionParameterImpl<StacktraceParameter.Stacktrace> implements CommandLineGradleExecutionParameter<StacktraceParameter.Stacktrace> {
    public enum Stacktrace {
        HIDE, SHOW
    }

    public static StacktraceParameter hide() {
        return fixed(StacktraceParameter.class, Stacktrace.HIDE);
    }

    public static StacktraceParameter show() {
        return fixed(StacktraceParameter.class, Stacktrace.SHOW);
    }

    @Override
    public List<String> getAsArguments() {
        if (get().equals(Stacktrace.SHOW)) {
            return Arrays.asList("--stacktrace");
        }
        return Collections.emptyList();
    }
}
