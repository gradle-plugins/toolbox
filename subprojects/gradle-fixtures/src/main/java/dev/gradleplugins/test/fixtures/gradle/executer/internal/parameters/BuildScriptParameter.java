package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Deprecated
public final class BuildScriptParameter extends GradleExecutionParameterImpl<RegularFile> implements CommandLineGradleExecutionParameter<RegularFile>, GradleExecutionParameter<RegularFile> {

    public static BuildScriptParameter unset() {
        return noValue(BuildScriptParameter.class);
    }

    public static BuildScriptParameter of(File buildScript) {
        return fixed(BuildScriptParameter.class, () -> buildScript);
    }

    @Override
    public List<String> getAsArguments() {
        if (isPresent()) {
            return Arrays.asList("--build-file", get().getAbsolutePath());
        }
        return Collections.emptyList();
    }
}
