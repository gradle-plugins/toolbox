package dev.gradleplugins.fixtures.gradle.runner.parameters;

public enum GradleExecutorType {
    UNKNOWN,
    GRADLE_TEST_KIT,
    GRADLE_WRAPPER;

    boolean isGradleTestKit() {
        return equals(GRADLE_TEST_KIT);
    }

    boolean isGradleWrapper() {
        return equals(GRADLE_WRAPPER);
    }
}
