package dev.gradleplugins.fixtures.gradle.runner;

import lombok.*;

import java.util.Objects;

@ToString
@EqualsAndHashCode
final class GradleBuildTaskImpl implements GradleBuildTask {
    @NonNull private final GradleTaskPath path;
    @Getter @NonNull private final GradleTaskOutcome outcome;
    @Getter @NonNull private final String output;

    GradleBuildTaskImpl(GradleTaskPath path, GradleTaskOutcome outcome, String output) {
        this.path = path;
        this.outcome = outcome;
        this.output = output;
    }

    @Override
    public String getPath() {
        return path.get();
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
            return new GradleBuildTaskImpl(Objects.requireNonNull(path), outcome, outputBuilder.toString());
        }
    }
}
