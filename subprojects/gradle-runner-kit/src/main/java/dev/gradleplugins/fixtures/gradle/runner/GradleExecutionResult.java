package dev.gradleplugins.fixtures.gradle.runner;

public interface GradleExecutionResult {

    String getOutput();

    boolean isSuccessful();
}
