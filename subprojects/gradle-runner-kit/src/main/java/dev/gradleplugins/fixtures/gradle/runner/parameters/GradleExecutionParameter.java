package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface GradleExecutionParameter<T> {
    default T get() {
        return (T) this;
    }

    default T orElse(T other) {
        return (T) this;
    }

    default T orElseGet(Supplier<T> supplier) {
        return (T) this;
    }

    default boolean isPresent() {
        return true;
    }

    default <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return Optional.of((T) this).map(mapper);
    }
}
