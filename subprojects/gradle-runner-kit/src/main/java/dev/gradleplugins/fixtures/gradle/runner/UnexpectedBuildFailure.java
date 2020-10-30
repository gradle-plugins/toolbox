package dev.gradleplugins.fixtures.gradle.runner;

/**
 * Thrown when executing a build that was expected to succeed, but failed.
 *
 * @see GradleRunner#build()
 */
public final class UnexpectedBuildFailure extends UnexpectedBuildResultException {
    public UnexpectedBuildFailure(String message, GradleBuildResult buildResult) {
        super(message, buildResult);
    }
}