package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class UnsetParameter<T> {
    public List<String> getAsArguments() {
        return Collections.emptyList();
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
