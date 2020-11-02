package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public enum DeprecationChecks implements GradleExecutionCommandLineParameter<DeprecationChecks> {
    FAILS(asList("--warning-mode", "fail")),
    IGNORES(emptyList());

    private final List<String> args;

    DeprecationChecks(List<String> args) {
        this.args = args;
    }

    @Override
    public List<String> getAsArguments() {
        return args;
    }
}
