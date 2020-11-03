package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@ToString
@EqualsAndHashCode
abstract class AbstractGradleExecutionProvider<T> implements GradleExecutionProviderInternal<T> {
    private static final ThreadLocal<GradleExecutionProvider<?>> NEXT_PARAMETER = new ThreadLocal<>();
    private final GradleExecutionProvider<T> value;

    @SuppressWarnings("unchecked")
    protected AbstractGradleExecutionProvider() {
        this.value = (GradleExecutionProvider<T>) NEXT_PARAMETER.get();
        NEXT_PARAMETER.remove();
    }

    public T get() {
        return value.get();
    }

    public T orElse(T other) {
        return value.orElse(other);
    }

    public T orElseGet(Supplier<T> supplier) {
        return value.orElseGet(supplier);
    }

    public boolean isPresent() {
        return value.isPresent();
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return value.map(mapper);
    }

    public void calculateValue(GradleExecutionContext context) {
        if (value instanceof CalculatedValueProvider) {
            ((CalculatedValueProvider<T>) value).calculateValue(context);
        }
    }

    protected static <T extends GradleExecutionProvider<?>> T noValue(Class<T> type) {
        NEXT_PARAMETER.set(new NoValueProvider<>(type));
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e); // TODO:
        }
    }

    protected static <T extends GradleExecutionProvider<S>, S> T fixed(Class<T> type, S value) {
        NEXT_PARAMETER.set(new FixedValueProvider<S>(value));
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e); // TODO:
        }
    }

    protected static <T extends GradleExecutionProvider<S>, S> T supplied(Class<T> type, Supplier<S> value) {
        NEXT_PARAMETER.set(new SuppliedValueProvider<S>(value));
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e); // TODO:
        }
    }

    protected static <T extends GradleExecutionProvider<S>, S> T calculated(Class<T> type, Function<GradleExecutionContext, S> value) {
        NEXT_PARAMETER.set(new CalculatedValueProvider<>(value));
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e); // TODO:
        }
    }

    @EqualsAndHashCode
    private static final class NoValueProvider<T> implements GradleExecutionProvider<T> {
        @EqualsAndHashCode.Exclude private final Class<T> type;

        public NoValueProvider(Class<T> type) {
            this.type = type;
        }

        @Override
        public T get() {
            throw new UnsupportedOperationException(String.format("Cannot get value for '%s'.", type.getSimpleName()));
        }

        @Override
        public T orElse(T other) {
            return other;
        }

        @Override
        public T orElseGet(Supplier<T> supplier) {
            return supplier.get();
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "no value";
        }
    }

    @EqualsAndHashCode
    private static class FixedValueProvider<T> implements GradleExecutionProvider<T> {
        private final T value;

        FixedValueProvider(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T orElse(T other) {
            return value;
        }

        @Override
        public T orElseGet(Supplier<T> supplier) {
            return value;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
            return Optional.of(value).map(mapper);
        }

        @Override
        public String toString() {
            return "fixed(" + value + ")";
        }
    }

    @EqualsAndHashCode
    private static class SuppliedValueProvider<T> implements GradleExecutionProvider<T> {
        private final Supplier<T> value;

        SuppliedValueProvider(Supplier<T> value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value.get();
        }

        @Override
        public T orElse(T other) {
            return value.get();
        }

        @Override
        public T orElseGet(Supplier<T> supplier) {
            return value.get();
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
            return Optional.of(value.get()).map(mapper);
        }

        @Override
        public String toString() {
            return "supplied(" + value + ")";
        }
    }

    @EqualsAndHashCode
    private static class CalculatedValueProvider<T> implements GradleExecutionProvider<T> {
        private final Function<GradleExecutionContext, T> generator;
        private T value;

        CalculatedValueProvider(Function<GradleExecutionContext, T> generator) {
            this.generator = generator;
        }

        @Override
        public T get() {
            assertValueCalculated();
            return value;
        }

        @Override
        public T orElse(T other) {
            assertValueCalculated();
            return value;
        }

        @Override
        public T orElseGet(Supplier<T> supplier) {
            assertValueCalculated();
            return value;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
            assertValueCalculated();
            return Optional.of(value).map(mapper);
        }

        private void assertValueCalculated() {
            if (value == null) {
                throw new RuntimeException("value not calculated");
            }
        }

        public void calculateValue(GradleExecutionContext context) {
            value = generator.apply(context);
        }

        @Override
        public String toString() {
            return "calculated(" + value + ")";
        }
    }
}
