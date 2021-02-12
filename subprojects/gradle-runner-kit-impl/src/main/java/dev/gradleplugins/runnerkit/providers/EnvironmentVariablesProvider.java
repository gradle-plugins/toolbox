package dev.gradleplugins.runnerkit.providers;

import lombok.val;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public final class EnvironmentVariablesProvider extends AbstractGradleExecutionProvider<Map<String, ?>> {

    public static EnvironmentVariablesProvider inherited() {
        return noValue(EnvironmentVariablesProvider.class);
    }

    public static EnvironmentVariablesProvider of(Map<String, ?> environmentVariables) {
        return fixed(EnvironmentVariablesProvider.class, environmentVariables);
    }

    public EnvironmentVariablesProvider plus(Map<String, ?> environmentVariables) {
        val values = new HashMap<String, Object>();
        if (isPresent()) {
            values.putAll(get());
        } else {
            values.putAll(System.getenv());
        }
        values.putAll(environmentVariables);
        return fixed(EnvironmentVariablesProvider.class, unmodifiableMap(values));
    }
}
