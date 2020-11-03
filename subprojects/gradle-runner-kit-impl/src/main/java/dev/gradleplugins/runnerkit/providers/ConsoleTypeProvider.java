package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public final class ConsoleTypeProvider extends AbstractGradleExecutionProvider<GradleExecutionContext.ConsoleType> implements GradleExecutionCommandLineProvider {
    public static ConsoleTypeProvider defaultConsole() {
        return noValue(ConsoleTypeProvider.class);
    }

    public static ConsoleTypeProvider richConsole() {
        return fixed(ConsoleTypeProvider.class, GradleExecutionContext.ConsoleType.RICH);
    }

    @Override
    public List<String> getAsArguments() {
        if (isPresent() && get().equals(GradleExecutionContext.ConsoleType.RICH)) {
            return asList("--console", "rich");
        }
        return emptyList();
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("--console"))) {
            if (context.getArguments().get().stream().anyMatch(it -> it.endsWith("rich"))) {
                if (isPresent()) {
                    throw new InvalidRunnerConfigurationException("Please remove command line flags for rich console, rich console is already enabled.");
                }
                throw new InvalidRunnerConfigurationException("Please use GradleRunner#withRichConsoleEnabled() instead of using the command line flags.");
            }
            throw new InvalidRunnerConfigurationException("Please open an issue on gradle-plugins/toolbox GitHub repository to support your use case.");
        }
    }
}
