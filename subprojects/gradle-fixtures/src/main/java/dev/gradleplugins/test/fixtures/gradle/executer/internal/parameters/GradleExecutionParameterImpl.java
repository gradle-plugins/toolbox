package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.lang.reflect.InvocationTargetException;

@Deprecated
public class GradleExecutionParameterImpl<T> {
    private static final ThreadLocal<GradleExecutionParameter<?>> NEXT_PARAMETER = new ThreadLocal<>();
    private final GradleExecutionParameter<T> value;

    @SuppressWarnings("unchecked")
    public GradleExecutionParameterImpl() {
        this.value = (GradleExecutionParameter<T>) NEXT_PARAMETER.get();
        NEXT_PARAMETER.remove();
    }

    public T get() {
        return value.get();
    }

    public T orElse(T other) {
        return value.orElse(other);
    }

    public boolean isPresent() {
        return value.isPresent();
    }

    static <T extends GradleExecutionParameter<?>> T noValue(Class<T> type) {
        NEXT_PARAMETER.set(new NoValueProvider<>());
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e); // TODO:
        }
    }

    static <T extends GradleExecutionParameter<S>, S> T fixed(Class<T> type, S value) {
        NEXT_PARAMETER.set(new FixedValueProvider<S>(value));
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e); // TODO:
        }
    }

    static class NoValueProvider<T> implements GradleExecutionParameter<T> {

        @Override
        public T get() {
            throw new UnsupportedOperationException();
        }

        @Override
        public T orElse(T other) {
            return other;
        }

        @Override
        public boolean isPresent() {
            return false;
        }
    }

    static class FixedValueProvider<T> implements GradleExecutionParameter<T> {
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
        public boolean isPresent() {
            return true;
        }
    }
}
