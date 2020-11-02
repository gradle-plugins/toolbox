package dev.gradleplugins.fixtures.gradle.runner;

import lombok.*;

import java.util.Objects;

/**
 * A task that was executed when running a specific build.
 *
 * @see GradleBuildResult
 */
@ToString
@EqualsAndHashCode
public final class GradleBuildTask {
    @NonNull private final GradleTaskPath path;
    @NonNull private final GradleTaskOutcome outcome;
    @NonNull private final String output;

    GradleBuildTask(GradleTaskPath path, GradleTaskOutcome outcome, String output) {
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
    public GradleTaskOutcome getOutcome() {
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
        private GradleTaskOutcome outcome = GradleTaskOutcome.SUCCESS;
        private GradleTaskPath path;
        private final StringBuilder outputBuilder = new StringBuilder();

        Builder withOutcome(GradleTaskOutcome outcome) {
            this.outcome = outcome;
            return this;
        }

        Builder withPath(GradleTaskPath path) {
            this.path = path;
            return this;
        }

        Builder appendToOutput(String output) {
            outputBuilder.append(output);
            return this;
        }

        GradleBuildTask build() {
            return new GradleBuildTask(Objects.requireNonNull(path), outcome, outputBuilder.toString());
        }
    }
}
