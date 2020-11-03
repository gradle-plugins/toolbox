package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public final class StacktraceProvider extends AbstractGradleExecutionProvider<GradleExecutionContext.Stacktrace> implements GradleExecutionCommandLineProvider {
    public static StacktraceProvider hide() {
        return fixed(StacktraceProvider.class, GradleExecutionContext.Stacktrace.HIDE);
    }

    public static StacktraceProvider show() {
        return fixed(StacktraceProvider.class, GradleExecutionContext.Stacktrace.SHOW);
    }

    @Override
    public List<String> getAsArguments() {
        if (get().equals(GradleExecutionContext.Stacktrace.SHOW)) {
            return singletonList("--stacktrace");
        }
        return emptyList();
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.equals("--stacktrace") || it.equals("-s"))) {
            throw new InvalidRunnerConfigurationException("Please remove stacktrace command line flags as showing the stacktrace is the default behavior of all toolbox runner.");
        }
    }
}
