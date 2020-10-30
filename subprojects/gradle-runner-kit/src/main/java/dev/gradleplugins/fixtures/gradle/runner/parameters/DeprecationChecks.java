package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum DeprecationChecks implements GradleExecutionCommandLineParameter<DeprecationChecks> {
    FAILS(ImmutableList.of("--warning-mode", "fail")),
    IGNORES(ImmutableList.of());

    private final List<String> args;

    DeprecationChecks(List<String> args) {
        this.args = args;
    }

    @Override
    public List<String> getAsArguments() {
        return args;
    }
}
