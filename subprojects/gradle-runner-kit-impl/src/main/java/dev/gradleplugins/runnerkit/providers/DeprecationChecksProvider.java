package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public final class DeprecationChecksProvider extends AbstractGradleExecutionProvider<GradleExecutionContext.DeprecationChecks> implements GradleExecutionCommandLineProvider {
    public static DeprecationChecksProvider fails() {
        return fixed(DeprecationChecksProvider.class, GradleExecutionContext.DeprecationChecks.FAILS);
    }

    public static DeprecationChecksProvider ignores() {
        return noValue(DeprecationChecksProvider.class);
    }

    @Override
    public List<String> getAsArguments() {
        if (isPresent() && get().equals(GradleExecutionContext.DeprecationChecks.FAILS)) {
            return asList("--warning-mode", "fail");
        }
        return emptyList();
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("--warning-mode"))) {
            if (context.getArguments().get().stream().anyMatch(it -> it.endsWith("all") || it.endsWith("none"))) {
                throw new InvalidRunnerConfigurationException("Please open an issue on gradle-plugins/toolbox GitHub repository to support your use case.");
            }

            if (context.getArguments().get().stream().anyMatch(it -> it.endsWith("fails"))) {
                if (isPresent()) {
                    throw new InvalidRunnerConfigurationException("Please remove command line flag for failing warning mode as it is the default for all toolbox runner.");
                }
                throw new InvalidRunnerConfigurationException("Please remove command line flag for failing warning mode and any calls to GradleRunner#withoutDeprecationChecks() for this runner as it is the default for all toolbox runner.");
            } else if (context.getArguments().get().stream().anyMatch(it -> it.endsWith("summary"))) {
                if (isPresent()) {
                    throw new InvalidRunnerConfigurationException("Please use GradleRunner#withoutDeprecationChecks() instead of using the command line flags.");
                }
                throw new InvalidRunnerConfigurationException("Please remove command line flag for summary warning mode as GradleRunner#withoutDeprecationChecks() already configure summary warning mode.");
            }
        }
    }
}
