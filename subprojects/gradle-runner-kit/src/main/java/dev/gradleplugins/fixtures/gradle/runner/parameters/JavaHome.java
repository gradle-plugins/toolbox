package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableMap;
import org.gradle.internal.jvm.Jvm;

import java.io.File;
import java.util.Map;

public class JavaHome extends GradleExecutionParameterImpl<File> implements GradleExecutionParameter<File>, GradleExecutionEnvironmentVariableParameter {
    public static JavaHome current() {
        return fixed(JavaHome.class, Jvm.current().getJavaHome());
    }

    @Override
    public Map<String, String> getAsEnvironmentVariables() {
        return map(JavaHome::asEnvironmentVariables).orElseGet(ImmutableMap::of);
    }

    private static Map<String, String> asEnvironmentVariables(File javaHomeDirectory) {
        return ImmutableMap.of("JAVA_HOME", javaHomeDirectory.getAbsolutePath());
    }
}
