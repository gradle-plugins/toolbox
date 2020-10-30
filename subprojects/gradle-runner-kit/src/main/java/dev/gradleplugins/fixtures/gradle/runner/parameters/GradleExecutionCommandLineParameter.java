package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.util.List;

public interface GradleExecutionCommandLineParameter<T> extends GradleExecutionParameter<T> {
    List<String> getAsArguments();
}
