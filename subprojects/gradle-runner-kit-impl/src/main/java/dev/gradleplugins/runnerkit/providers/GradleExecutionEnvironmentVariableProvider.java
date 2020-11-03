package dev.gradleplugins.runnerkit.providers;

import java.util.Map;

public interface GradleExecutionEnvironmentVariableProvider {
    Map<String, String> getAsEnvironmentVariables();
}
