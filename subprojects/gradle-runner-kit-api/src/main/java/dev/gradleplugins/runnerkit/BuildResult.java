package dev.gradleplugins.runnerkit;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface BuildResult {
    String getOutput();

    List<String> getExecutedTaskPaths();

    List<String> getSkippedTaskPaths();

    static BuildResult from(String output) {
        return ClassUtils.staticInvoke("dev.gradleplugins.runnerkit.BuildResultImpl", "from", new Class[] {String.class}, output);
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

    BuildResult withNormalizedTaskOutput(Predicate<TaskPath> predicate, UnaryOperator<String> outputNormalizer);

    BuildResult asRichOutputResult();
}
