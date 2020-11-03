package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;

public final class UserHomeDirectoryProvider extends AbstractGradleExecutionProvider<File> implements GradleExecutionJvmSystemPropertyProvider {
    public static UserHomeDirectoryProvider implicit() {
        return noValue(UserHomeDirectoryProvider.class);
    }

    public static UserHomeDirectoryProvider of(File userHomeDirectory) {
        return fixed(UserHomeDirectoryProvider.class, userHomeDirectory);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return map(UserHomeDirectoryProvider::asJvmSystemProperties).orElseGet(Collections::emptyMap);
    }

    private static Map<String, String> asJvmSystemProperties(File userHomeDirectory) {
        return singletonMap("user.home", userHomeDirectory.getAbsolutePath());
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("-Duser.home"))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#withUserHomeDirectory(File) instead of using the command line flags.");
        }
    }
}
