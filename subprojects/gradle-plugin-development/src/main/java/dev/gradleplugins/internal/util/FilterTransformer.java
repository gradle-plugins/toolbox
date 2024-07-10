package dev.gradleplugins.internal.util;

import org.gradle.api.Transformer;
import org.gradle.api.specs.Spec;

public final class FilterTransformer<T> implements Transformer<T, T> {
    private final Spec<? super T> spec;

    private FilterTransformer(Spec<? super T> spec) {
        this.spec = spec;
    }

    @Override
    public T transform(T t) {
        if (spec.isSatisfiedBy(t)) {
            return t;
        }
        return null;
    }

    public static <T> FilterTransformer<T> filter(Spec<? super T> spec) {
        return new FilterTransformer<>(spec);
    }
}
