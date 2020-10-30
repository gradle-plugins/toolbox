package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum BuildCache implements GradleExecutionCommandLineParameter<BuildCache> {
    ENABLED(ImmutableList.of("--build-cache")), DISABLED(ImmutableList.of());

    private final List<String> args;

    BuildCache(List<String> args) {
        this.args = args;
    }

    @Override
    public List<String> getAsArguments() {
        return args;
    }
}
