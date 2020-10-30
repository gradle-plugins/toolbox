package dev.gradleplugins.fixtures.gradle.runner;

public abstract class UnexpectedBuildResultException extends RuntimeException {
    private final GradleBuildResult buildResult;

    UnexpectedBuildResultException(String message, GradleBuildResult buildResult) {
        super(message);
        this.buildResult = buildResult;
    }

    public GradleBuildResult getBuildResult() {
        return this.buildResult;
    }
}