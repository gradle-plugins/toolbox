package dev.gradleplugins.fixtures.gradle.runner.parameters;

import dev.gradleplugins.fixtures.gradle.runner.GradleExecutionContext;

public interface GradleExecutionParameterInternal {
    void calculateValue(GradleExecutionContext context);
}
