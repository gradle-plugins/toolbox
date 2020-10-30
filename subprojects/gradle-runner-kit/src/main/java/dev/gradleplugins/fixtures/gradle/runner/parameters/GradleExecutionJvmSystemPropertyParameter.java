package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface GradleExecutionJvmSystemPropertyParameter<T> extends GradleExecutionCommandLineParameter<T> {
    default List<String> getAsArguments() {
        return getAsJvmSystemProperties().entrySet().stream().map(it -> "-D" + it.getKey() + "=" + it.getValue()).collect(Collectors.toList());
    }

    Map<String, String> getAsJvmSystemProperties();
}
