package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.Value;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Deprecated
public interface EnvironmentVariablesParameter {
    EnvironmentVariablesParameter plus(Map<String, ?> environmentVariables);

    boolean isEmpty();

    Map<String, String> getAsMap();

    static EnvironmentVariablesParameter empty() {
        return new EmptyEnvironmentVariablesParameter();
    }

    @Value
    class EmptyEnvironmentVariablesParameter implements EnvironmentVariablesParameter {
        @Override
        public EnvironmentVariablesParameter plus(Map<String, ?> environmentVariables) {
            if (environmentVariables.isEmpty()) {
                return this;
            }
            return new DefaultEnvironmentVariablesParameter(Collections.unmodifiableMap(environmentVariables));
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Map<String, String> getAsMap() {
            return System.getenv();
        }
    }

    @Value
    class DefaultEnvironmentVariablesParameter implements EnvironmentVariablesParameter {
        Map<String, ?> value;

        @Override
        public EnvironmentVariablesParameter plus(Map<String, ?> environmentVariables) {
            if (environmentVariables.isEmpty()) {
                return this;
            }
            return new DefaultEnvironmentVariablesParameter(Collections.unmodifiableMap(new HashMap<String, Object>() {{
                putAll(value);
                putAll(environmentVariables);
            }}));
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Map<String, String> getAsMap() {
            return value.entrySet().stream().map(it -> new AbstractMap.SimpleImmutableEntry<>(it.getKey(), it.getValue().toString())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
