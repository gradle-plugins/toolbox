package dev.gradleplugins.runnerkit.providers;

import lombok.val;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public final class EnvironmentVariablesProvider extends AbstractGradleExecutionProvider<Map<String, ?>> {

    public static EnvironmentVariablesProvider contextDefault() {
        return noValue(EnvironmentVariablesProvider.class);
    }

    public EnvironmentVariablesProvider plus(Map<String, ?> environmentVariables) {
        if (isPresent()) {
            val values = new HashMap<String, Object>();
            values.putAll(get());
            values.putAll(environmentVariables);
            return fixed(EnvironmentVariablesProvider.class, unmodifiableMap(values));
        }
        return fixed(EnvironmentVariablesProvider.class, unmodifiableMap(environmentVariables));
    }
}
