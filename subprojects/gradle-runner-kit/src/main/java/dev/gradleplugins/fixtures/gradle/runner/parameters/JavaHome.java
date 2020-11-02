package dev.gradleplugins.fixtures.gradle.runner.parameters;

import org.gradle.internal.jvm.Jvm;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;

public class JavaHome extends GradleExecutionParameterImpl<File> implements GradleExecutionParameter<File>, GradleExecutionEnvironmentVariableParameter {
    public static JavaHome current() {
        return fixed(JavaHome.class, Jvm.current().getJavaHome());
    }

    @Override
    public Map<String, String> getAsEnvironmentVariables() {
        return map(JavaHome::asEnvironmentVariables).orElseGet(Collections::emptyMap);
    }

    private static Map<String, String> asEnvironmentVariables(File javaHomeDirectory) {
        return singletonMap("JAVA_HOME", javaHomeDirectory.getAbsolutePath());
    }
}
