package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;

public final class BuildScript extends GradleExecutionParameterImpl<File> implements GradleExecutionCommandLineParameter<File> {
    public static BuildScript unset() {
        return noValue(BuildScript.class);
    }

    public static BuildScript of(File buildScript) {
        return fixed(BuildScript.class, buildScript);
    }

    @Override
    public List<String> getAsArguments() {
        return map(BuildScript::asArguments).orElseGet(ImmutableList::of);
    }

    private static List<String> asArguments(File buildScript) {
        return ImmutableList.of("--build-file", buildScript.getAbsolutePath());
    }
}
