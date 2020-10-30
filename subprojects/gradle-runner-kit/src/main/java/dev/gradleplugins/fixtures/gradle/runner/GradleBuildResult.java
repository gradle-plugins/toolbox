package dev.gradleplugins.fixtures.gradle.runner;

import dev.nokee.core.exec.CommandLineToolLogContent;
import org.gradle.api.invocation.Gradle;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface GradleBuildResult {
    String getOutput();

    List<String> getExecutedTaskPaths();

    List<String> getSkippedTaskPaths();

    static GradleBuildResult from(String output) {
        return GradleBuildResultImpl.from(CommandLineToolLogContent.of(output));
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
    List<GradleBuildTask> getTasks();

    /**
     * The subset of {@link #getTasks()} that had the given outcome.
     * <p>
     * The returned list is an unmodifiable view of items.
     * The returned list will be empty if no tasks were executed with the given outcome.
     *
     * @param outcome the desired outcome
     * @return the build tasks with the given outcome
     */
    List<GradleBuildTask> tasks(GradleTaskOutcome outcome);

    /**
     * Returns the result object for a particular task, or {@code null} if the given task was not part of the build.
     *
     * @param taskPath the path of the target task
     * @return information about the executed task, or {@code null} if the task was not executed
     */
    @Nullable
    GradleBuildTask task(String taskPath);

    GradleBuildResult withNormalizedTaskOutput(Predicate<GradleTaskPath> predicate, UnaryOperator<String> outputNormalizer);

    GradleBuildResult asRichOutputResult();
}
