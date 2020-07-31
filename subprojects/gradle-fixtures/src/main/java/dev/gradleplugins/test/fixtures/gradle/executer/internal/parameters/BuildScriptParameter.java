package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public interface BuildScriptParameter extends CommandLineGradleParameter, RegularFileParameter {
    List<String> getAsArguments();

    static BuildScriptParameter unset() {
        return new UnsetBuildScriptParameter();
    }

    static BuildScriptParameter of(File buildScript) {
        return new DefaultBuildScriptParameter(buildScript);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class UnsetBuildScriptParameter extends UnsetParameter<File> implements BuildScriptParameter {}

    @Value
    class DefaultBuildScriptParameter implements BuildScriptParameter {
        @NonNull File value;

        @Override
        public List<String> getAsArguments() {
            return Arrays.asList("--build-file", value.getAbsolutePath());
        }
    }
}
