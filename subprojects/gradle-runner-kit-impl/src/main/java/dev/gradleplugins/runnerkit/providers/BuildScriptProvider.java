package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BuildScriptProvider extends AbstractGradleExecutionProvider<File> implements GradleExecutionCommandLineProvider {
    public static BuildScriptProvider unset() {
        return noValue(BuildScriptProvider.class);
    }

    public static BuildScriptProvider of(File buildScript) {
        return fixed(BuildScriptProvider.class, buildScript);
    }

    @Override
    public List<String> getAsArguments() {
        return map(BuildScriptProvider::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(File buildScript) {
        return Arrays.asList("--build-file", buildScript.getAbsolutePath());
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.equals("-b") || it.startsWith("--build-file"))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#usingBuildScript(File) instead of using the command line flags.");
        }
    }
}
