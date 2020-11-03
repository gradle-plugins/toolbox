package dev.gradleplugins.runnerkit.providers;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface GradleExecutionProvider<T> {
    T get();

    T orElse(T other);

    T orElseGet(Supplier<T> supplier);

    boolean isPresent();

    <U> Optional<U> map(Function<? super T, ? extends U> mapper);
}
