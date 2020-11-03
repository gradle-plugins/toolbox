package dev.gradleplugins.runnerkit;

/**
 * Thrown when a build cannot be executed due to the runner being in an invalid state.
 *
 * @see GradleRunner#build()
 * @see GradleRunner#buildAndFail()
 */
public final class InvalidRunnerConfigurationException extends IllegalStateException {
    public InvalidRunnerConfigurationException(String s) {
        super(s);
    }

    public InvalidRunnerConfigurationException(String s, RuntimeException e) {
        super(s, e);
    }
}