package dev.gradleplugins.fixtures.gradle.runner;

import org.gradle.testkit.runner.TaskOutcome;

/**
 * A task that was executed when running a specific build.
 *
 * @see GradleBuildResult
 */
public interface GradleBuildTask {

    /**
     * The unique path of the task.
     * <p>
     * The task path is a combination of its enclosing project's path and its name.
     * For example, in multi project build the {@code bar} task of the {@code foo} project has a path of {@code :foo:bar}.
     * In a single project build, the {@code bar} task of the lone project has a path of {@code :bar}.
     * <p>
     * This value corresponds to the value output by Gradle for the task during its normal progress logging.
     *
     * @return the task path
     */
    String getPath();

    /**
     * The outcome of attempting to execute this task.
     *
     * @return the task outcome
     */
    GradleTaskOutcome getOutcome();

    /**
     * The plain output of the task during the build.
     *
     * @return the task output during the build.
     */
    String getOutput();
}