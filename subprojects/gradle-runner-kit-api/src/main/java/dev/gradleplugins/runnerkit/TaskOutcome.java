package dev.gradleplugins.runnerkit;

/**
 * The outcome of executing a task during a build.
 *
 * @see BuildTask#getOutcome()
 */
public enum TaskOutcome {
    /**
     * The task executed and performed its actions without failure.
     */
    SUCCESS,

    /**
     * The task attempted to execute, but did not complete successfully.
     */
    FAILED,

    /**
     * The task was not executed, as its output was up to date.
     */
    UP_TO_DATE,

    /**
     * The task was not executed due to some reason.
     *
     * For Gradle version &lt; 3.4, a task may be skipped if it had no work to do (e.g. no source to compile).
     */
    SKIPPED,

    /**
     * The task executed, but did not perform work as its output was found in a build cache.
     * <p>
     * This outcome only occurs when the build under test has been configured for
     * <a href="https://docs.gradle.org/current/userguide/build_cache.html#sec:task_output_caching" target="_top">task output caching</a>.
     * </p>
     * <p>NOTE: If the Gradle version used for the build under test is older than 3.3,
     * no tasks will have this outcome.</p>
     */
    FROM_CACHE,

    /**
     * The task was skipped due to all input files declared with {@code @SkipWhenEmpty} being empty.
     *<p>NOTE: If the Gradle version used for the build under test is older than 3.4,
     * no tasks will have this outcome.</p>
     */
    NO_SOURCE;
}