package dev.gradleplugins.fixtures.gradle.runner.parameters;

import dev.gradleplugins.fixtures.gradle.runner.GradleExecutionContext;

import java.util.function.Consumer;

public interface BeforeExecute extends Consumer<GradleExecutionContext> {
}
