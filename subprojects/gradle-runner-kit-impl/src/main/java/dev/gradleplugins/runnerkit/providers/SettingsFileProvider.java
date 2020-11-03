package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public final class SettingsFileProvider extends AbstractGradleExecutionProvider<File> implements GradleExecutionCommandLineProvider {
    public static SettingsFileProvider unset() {
        return noValue(SettingsFileProvider.class);
    }

    public static SettingsFileProvider of(File settingsFile) {
        return fixed(SettingsFileProvider.class, settingsFile);
    }

    @Override
    public List<String> getAsArguments() {
        return map(SettingsFileProvider::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(File settingsFile) {
        return asList("--settings-file", settingsFile.getAbsolutePath());
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("--settings-file") || it.equals("-c"))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#usingSettingsFile(File) instead of using the command line flags.");
        }
    }
}
