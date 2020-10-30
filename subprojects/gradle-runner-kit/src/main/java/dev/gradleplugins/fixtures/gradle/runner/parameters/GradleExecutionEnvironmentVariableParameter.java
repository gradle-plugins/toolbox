package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.util.Map;

public interface GradleExecutionEnvironmentVariableParameter {
    Map<String, String> getAsEnvironmentVariables();
}
