package dev.gradleplugins.runnerkit;

public abstract class UnexpectedBuildResultException extends RuntimeException {
    private final BuildResult buildResult;

    UnexpectedBuildResultException(String message, BuildResult buildResult) {
        super(message);
        this.buildResult = buildResult;
    }

    public BuildResult getBuildResult() {
        return this.buildResult;
    }
}