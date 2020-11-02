package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public enum Stacktrace implements GradleExecutionCommandLineParameter<Stacktrace> {
    HIDE(emptyList()),
    SHOW(singletonList("--stacktrace"));

    private final List<String> args;

    Stacktrace(List<String> args) {
        this.args = args;
    }

    @Override
    public List<String> getAsArguments() {
        return args;
    }
}
