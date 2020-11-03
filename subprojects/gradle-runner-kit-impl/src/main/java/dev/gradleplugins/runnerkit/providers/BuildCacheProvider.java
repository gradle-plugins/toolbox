package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;
import org.hamcrest.Matchers;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;

public final class BuildCacheProvider extends AbstractGradleExecutionProvider<GradleExecutionContext.BuildCache> implements GradleExecutionCommandLineProvider {

    public static BuildCacheProvider enabled() {
        return fixed(BuildCacheProvider.class, GradleExecutionContext.BuildCache.ENABLED);
    }

    public static BuildCacheProvider disabled() {
        return fixed(BuildCacheProvider.class, GradleExecutionContext.BuildCache.DISABLED);
    }

    @Override
    public List<String> getAsArguments() {
        if (get().equals(GradleExecutionContext.BuildCache.ENABLED)) {
            return asList("--build-cache");
        }
        return emptyList();
    }

    @Override
    public void validate(GradleExecutionContext context) {
        // New values requires change to the logic of this provider
        assertThat(get(), Matchers.in(asList(GradleExecutionContext.BuildCache.ENABLED, GradleExecutionContext.BuildCache.DISABLED)));

        if (get().equals(GradleExecutionContext.BuildCache.ENABLED)) {
            if (context.getArguments().get().contains("--build-cache")) {
                throw new InvalidRunnerConfigurationException("Please remove command line flag enabling build cache as it was already enabled via GradleRunner#withBuildCacheEnabled().");
            } else if (context.getArguments().get().contains("--no-build-cache")) {
                throw new InvalidRunnerConfigurationException("Please remove command line flag disabling build cache and any call to GradleRunner#withBuildCacheEnabled() for this runner as it is disabled by default for all toolbox runner.");
            }
        } else if (get().equals(GradleExecutionContext.BuildCache.DISABLED)) {
            if (context.getArguments().get().contains("--build-cache")) {
                throw new InvalidRunnerConfigurationException("Please use GradleRunner#withBuildCacheEnabled() instead of using flag in command line arguments.");
            } else if (context.getArguments().get().contains("--no-build-cache")) {
                throw new InvalidRunnerConfigurationException("Please remove command line flag disabling build cache as it is disabled by default for all toolbox runner.");
            }
        }
    }
}
