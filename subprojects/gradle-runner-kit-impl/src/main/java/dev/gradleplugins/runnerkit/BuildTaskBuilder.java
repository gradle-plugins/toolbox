package dev.gradleplugins.runnerkit;

import java.util.Objects;

public final class BuildTaskBuilder {
    private TaskOutcome outcome = TaskOutcome.SUCCESS;
    private TaskPath path;
    private final StringBuilder outputBuilder = new StringBuilder();

    private BuildTaskBuilder() {}

    public static BuildTaskBuilder newBuilder() {
        return new BuildTaskBuilder();
    }

    public BuildTaskBuilder withOutcome(TaskOutcome outcome) {
        this.outcome = outcome;
        return this;
    }

    public BuildTaskBuilder withPath(TaskPath path) {
        this.path = path;
        return this;
    }

    public BuildTaskBuilder appendToOutput(String output) {
        outputBuilder.append(output);
        return this;
    }

    public BuildTaskImpl build() {
        return new BuildTaskImpl(Objects.requireNonNull(path), outcome, outputBuilder.toString());
    }
}