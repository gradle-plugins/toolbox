package dev.gradleplugins.runnerkit;

/**
 * Thrown when executing a build that was expected to fail, but succeeded.
 *
 * @see GradleRunner#buildAndFail()
 */
public final class UnexpectedBuildSuccess extends UnexpectedBuildResultException {
    public UnexpectedBuildSuccess(String message, BuildResult buildResult) {
        super(message, buildResult);
    }
}