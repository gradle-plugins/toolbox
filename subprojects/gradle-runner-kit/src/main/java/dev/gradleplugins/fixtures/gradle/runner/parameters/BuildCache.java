package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public enum BuildCache implements GradleExecutionCommandLineParameter<BuildCache> {
    ENABLED(asList("--build-cache")), DISABLED(emptyList());

    private final List<String> args;

    BuildCache(List<String> args) {
        this.args = args;
    }

    @Override
    public List<String> getAsArguments() {
        return args;
    }
}
