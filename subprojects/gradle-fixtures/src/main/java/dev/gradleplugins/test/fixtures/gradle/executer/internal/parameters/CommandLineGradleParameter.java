package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.Iterator;
import java.util.List;

public interface CommandLineGradleParameter extends Iterable<String> {
    List<String> getAsArguments();

    @Override
    default Iterator<String> iterator() {
        return getAsArguments().iterator();
    }
}
