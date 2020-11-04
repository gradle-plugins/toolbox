package dev.gradleplugins.runnerkit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
    @NonNull @Getter private final TaskOutcome outcome;
    @NonNull @Getter private final String output;

    BuildTaskImpl(TaskPath path, TaskOutcome outcome, String output) {
        this.path = path;
        this.outcome = outcome;
        this.output = output;
    }

    public String getPath() {
        return path.get();
    }

    void toString(StringBuilder result) {
        result.append("> Task ").append(getPath());
        if (!getOutput().isEmpty()) {
            result.append("\n").append(getOutput()).append("\n");

            if (!getOutcome().equals(TaskOutcome.SUCCESS)) {
                result.append("> Task ").append(getPath());
            }
        }

        if (!getOutcome().equals(TaskOutcome.SUCCESS)) {
            result.append(" ").append(TaskOutcomeUtils.toString(getOutcome()));
        }
    }
}
