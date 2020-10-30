package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.util.Collection;
import java.util.List;

interface GradleExecutionCollectionParameter<T extends Collection<?>> extends GradleExecutionParameter<T> {
    default boolean isEmpty() {
        return get().isEmpty();
    }

    default boolean contains(Object o) {
        return get().stream().anyMatch(o::equals);
    }
}
