package dev.gradleplugins.runnerkit;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface BuildResult {
    /**
     * The textual output produced during the build.
     * <p>
     * This is equivalent to the console output produced when running a build from the command line.
     * It contains both the standard output, and standard error output, of the build.
     *
     * @return the build output, or an empty string if there was no build output (e.g. ran with {@code -q})
     */
    String getOutput();

    /**
     * Returns a list of task paths for all executed tasks (e.g. any outcome).
     *
     * @return a list of task path, never null.
     */
    List<String> getExecutedTaskPaths();

    /**
     * Returns a list of task paths for all skipped tasks (e.g. up-to-date, no-source, skipped, and cached outcome).
     *
     * @return a list of task path, never null.
     */
    List<String> getSkippedTaskPaths();

    /**
     * Creates a build result from a textual output produced by a Gradle build.
     * The build result is limited by what is "seen" in the textual output.
     * Unfortunately, the same build result with different console (i.e. verbose vs quiet) will produce different build result.
     * We can use transformers to align build results by removing information from the result to create a common denominator.
     *
     * @param output the textual output produced by a Gradle build.
     * @return a {@link BuildResult} representing the output, never null.
     */
    static BuildResult from(String output) {
        return ClassUtils.staticInvoke("dev.gradleplugins.runnerkit.logging.GradleLogContentUtils", "scrapBuildResultFrom", new Class[] {String.class}, output);
    }

    /**
     * The tasks that were part of the build.
     * <p>
     * The order of the tasks corresponds to the order in which the tasks were started.
     * If executing a parallel enabled build, the order is not guaranteed to be deterministic.
     * <p>
     * The returned list is an unmodifiable view of items.
     * The returned list will be empty if no tasks were executed.
     * This can occur if the build fails early, due to a build script failing to compile for example.
     *
     * @return the build tasks
     */
    List<BuildTask> getTasks();

    /**
     * The subset of {@link #getTasks()} that had the given outcome.
     * <p>
     * The returned list is an unmodifiable view of items.
     * The returned list will be empty if no tasks were executed with the given outcome.
     *
     * @param outcome the desired outcome
     * @return the build tasks with the given outcome
     */
    List<BuildTask> tasks(TaskOutcome outcome);

    /**
     * Returns the result object for a particular task, or {@code null} if the given task was not part of the build.
     *
     * @param taskPath the path of the target task
     * @return information about the executed task, or {@code null} if the task was not executed
     */
    @Nullable
    BuildTask task(String taskPath);

    /**
     * Returns a new build result with the task output matching the predicate normalized by the output normalizer.
     *
     * @param predicate a predicate matching tasks based on their task path.
     * @param outputNormalizer an output normalizer to use for normalizing the task output.
     * @return a new {@link BuildResult} with the matching task output normalized, never null.
     */
    BuildResult withNormalizedTaskOutput(Predicate<TaskPath> predicate, UnaryOperator<String> outputNormalizer);

    /**
     * Returns a new build result without the tasks executed from the buildSrc included build.
     *
     * @return a new {@link BuildResult} without any tasks from buildSrc, never null.
     */
    BuildResult withoutBuildSrc();

    /**
     * Dilutes the build result to match a rich console, output scrapping, build result.
     * Every tasks without output will be drop without affecting the actionable task count.
     * It means the same build for a rich console or a verbose console will be equals.
     *
     * @return a new {@link BuildResult} without outputless tasks, never null.
     */
    BuildResult asRichOutputResult();

    /**
     * Returns the failures in this build result, if any.
     *
     * @return a list of failure description and causes for this build result or empty list if the build was successful.
     */
    List<Failure> getFailures();

    interface Failure {
        String getDescription();
        List<String> getCauses();
    }
}
