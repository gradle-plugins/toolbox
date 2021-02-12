package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;

public final class JavaHomeProvider extends AbstractGradleExecutionProvider<File> implements GradleExecutionEnvironmentVariableProvider {
    public static JavaHomeProvider current() {
        return fixed(JavaHomeProvider.class, SystemUtils.getJavaHome());
    }

    public static JavaHomeProvider inherited() {
        return noValue(JavaHomeProvider.class);
    }

    @Override
    public Map<String, String> getAsEnvironmentVariables() {
        return map(JavaHomeProvider::asEnvironmentVariables).orElseGet(Collections::emptyMap);
    }

    private static Map<String, String> asEnvironmentVariables(File javaHomeDirectory) {
        return singletonMap("JAVA_HOME", javaHomeDirectory.getAbsolutePath());
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getEnvironmentVariables().isPresent() && context.getEnvironmentVariables().get().containsKey("JAVA_HOME")) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#withJavaHomeDirectory(File) instead of using environment variables.");
        }
    }
}
