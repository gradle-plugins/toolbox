package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;

import java.util.function.Consumer;

public interface BeforeExecute extends Consumer<GradleExecutionContext> {
}
