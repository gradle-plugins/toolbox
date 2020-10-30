package dev.gradleplugins.fixtures.gradle.runner;

/**
 * Thrown when executing a build that was expected to fail, but succeeded.
 *
 * @see GradleRunner#buildAndFail()
 */
public final class UnexpectedBuildSuccess extends UnexpectedBuildResultException {
    public UnexpectedBuildSuccess(String message, GradleBuildResult buildResult) {
        super(message, buildResult);
    }
}