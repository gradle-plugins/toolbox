package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

@Deprecated
public interface GradleExecutionParameter<T> {
    T get();

    T orElse(T other);

    boolean isPresent();
}
