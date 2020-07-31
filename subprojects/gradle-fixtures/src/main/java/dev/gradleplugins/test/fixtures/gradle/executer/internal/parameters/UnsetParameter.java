package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UnsetParameter<T> {
    public List<String> getAsArguments() {
        return Collections.emptyList();
    }

    public Map<String, String> getAsJvmSystemProperties() {
        return Collections.emptyMap();
    }

    public boolean isPresent() {
        return false;
    }

    public T orElse(T other) {
        return other;
    }

    public File getAsFile() {
        throw new UnsupportedOperationException("The value of this parameter is unset");
    }
}
