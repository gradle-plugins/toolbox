package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum Stacktrace implements GradleExecutionCommandLineParameter<Stacktrace> {
    HIDE(ImmutableList.of()),
    SHOW(ImmutableList.of("--stacktrace"));

    private final List<String> args;

    Stacktrace(List<String> args) {
        this.args = args;
    }

    @Override
    public List<String> getAsArguments() {
        return args;
    }
}
