package dev.gradleplugins.runnerkit;

public interface GradleExecutor {
    GradleExecutionResult run(GradleExecutionContext parameters);

    /**
     * Creates a Gradle Test Kit executor.
     *
     * @return a {@link GradleExecutor} instance using the Gradle Test Kit API, never null.
     */
    static GradleExecutor gradleTestKit() {
        return ClassUtils.newInstance("dev.gradleplugins.runnerkit.GradleExecutorGradleTestKitImpl");
    }

    /**
     * Creates a Gradle Wrapper executor.
     *
     * @return a {@link GradleExecutor} instance forking a process using the Gradle wrapper script, never null.
     */
    static GradleExecutor gradleWrapper() {
        return ClassUtils.newInstance("dev.gradleplugins.runnerkit.GradleExecutorGradleWrapperImpl");
    }
}
