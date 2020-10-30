package dev.gradleplugins.fixtures.gradle.runner;

public interface GradleExecutor {
    GradleExecutionResult run(GradleExecutionContext parameters);

    /**
     * Creates a Gradle Test Kit executor.
     *
     * @return a {@link GradleExecutor} instance using the Gradle Test Kit API, never null.
     */
    static GradleExecutor gradleTestKit() {
        return new GradleExecutorGradleTestKitImpl();
    }

    /**
     * Creates a Gradle Wrapper executor.
     *
     * @return a {@link GradleExecutor} instance forking a process using the Gradle wrapper script, never null.
     */
    static GradleExecutor gradleWrapper() {
        return new GradleExecutorGradleWrapperImpl();
    }
}
