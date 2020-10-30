package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum ConsoleType implements GradleExecutionCommandLineParameter<ConsoleType>, GradleExecutionParameter<ConsoleType> {
    DEFAULT(ImmutableList.of()),
    /**
     * Enable color and rich output, regardless of whether the current process is attached to a console or not.
     * When not attached to a console, the color and rich output is encoded using ANSI control characters.
     */
    RICH(ImmutableList.of("--console", "rich"));

    private final List<String> args;

    ConsoleType(List<String> args) {
        this.args = args;
    }

    @Override
    public List<String> getAsArguments() {
        return args;
    }
}
