package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;

public interface GradleExecutionProviderInternal<T> extends GradleExecutionProvider<T> {
    void calculateValue(GradleExecutionContext context);

    default void validate(GradleExecutionContext context) {

    }
}
