package dev.gradleplugins.runnerkit;

/**
 * Thrown when executing a build that was expected to succeed, but failed.
 *
 * @see GradleRunner#build()
 */
public final class UnexpectedBuildFailure extends UnexpectedBuildResultException {
    public UnexpectedBuildFailure(String message, BuildResult buildResult) {
        super(message, buildResult);
    }
}