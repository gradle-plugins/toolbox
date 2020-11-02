package dev.gradleplugins.fixtures.gradle.runner.parameters;

import lombok.val;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public final class EnvironmentVariables extends GradleExecutionParameterImpl<Map<String, ?>> implements GradleExecutionParameter<Map<String, ?>> {

    public static EnvironmentVariables unset() {
        return noValue(EnvironmentVariables.class);
    }

    public EnvironmentVariables plus(Map<String, ?> environmentVariables) {
        if (isPresent()) {
            val values = new HashMap<String, Object>();
            values.putAll(get());
            values.putAll(environmentVariables);
            return fixed(EnvironmentVariables.class, unmodifiableMap(values));
        }
        return fixed(EnvironmentVariables.class, unmodifiableMap(environmentVariables));
    }
}
