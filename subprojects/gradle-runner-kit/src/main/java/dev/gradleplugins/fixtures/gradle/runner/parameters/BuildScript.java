package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
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
        return map(BuildScript::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(File buildScript) {
        return Arrays.asList("--build-file", buildScript.getAbsolutePath());
    }
}
