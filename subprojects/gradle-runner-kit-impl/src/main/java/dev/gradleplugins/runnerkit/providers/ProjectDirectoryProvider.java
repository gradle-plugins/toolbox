package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ProjectDirectoryProvider extends AbstractGradleExecutionProvider<File> implements GradleExecutionCommandLineProvider {
    @Override
    public List<String> getAsArguments() {
        return map(ProjectDirectoryProvider::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(File projectDirectory) {
        return Arrays.asList("--project-dir", projectDirectory.getAbsolutePath());
    }

    public static ProjectDirectoryProvider useWorkingDirectoryImplicitly() {
        return noValue(ProjectDirectoryProvider.class);
    }

    public static ProjectDirectoryProvider of(File projectDirectory) {
        return fixed(ProjectDirectoryProvider.class, projectDirectory);
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (!isPresent() && !context.getWorkingDirectory().isPresent()) {
            throw new InvalidRunnerConfigurationException("Please specify a working directory via GradleRunner#inDirectory(File) or a project directory via GradleRunner#usingProjectDirectory(File).");
        } else if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("--project-dir") || it.equals("-p"))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#usingProjectDirectory(File) instead of using the command line flags.");
        }
    }
}
