package dev.gradleplugins.runnerkit;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.Objects;

/**
 * A task that was executed when running a specific build.
 *
 * @see BuildResult
 */
@ToString
@EqualsAndHashCode
public final class BuildTaskImpl implements BuildTask {
    @NonNull private final TaskPath path;
    @NonNull private final TaskOutcome outcome;
    @NonNull private final String output;

    BuildTaskImpl(TaskPath path, TaskOutcome outcome, String output) {
        this.path = path;
        this.outcome = outcome;
        this.output = output;
    }

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
    public String getPath() {
        return path.get();
    }

    /**
     * The outcome of attempting to execute this task.
     *
     * @return the task outcome
     */
    public TaskOutcome getOutcome() {
        return outcome;
    }

    /**
     * The plain output of the task during the build.
     *
     * @return the task output during the build.
     */
    public String getOutput() {
        return output;
    }

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {
        private TaskOutcome outcome = TaskOutcome.SUCCESS;
        private TaskPath path;
        private final StringBuilder outputBuilder = new StringBuilder();

        Builder withOutcome(TaskOutcome outcome) {
            this.outcome = outcome;
            return this;
        }

        Builder withPath(TaskPath path) {
            this.path = path;
            return this;
        }

        Builder appendToOutput(String output) {
            outputBuilder.append(output);
            return this;
        }

        BuildTaskImpl build() {
            return new BuildTaskImpl(Objects.requireNonNull(path), outcome, outputBuilder.toString());
        }
    }
}
