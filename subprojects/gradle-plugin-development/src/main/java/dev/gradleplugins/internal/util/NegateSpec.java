package dev.gradleplugins.internal.util;

import org.gradle.api.specs.Spec;

public final class NegateSpec<T> implements Spec<T> {
    private final Spec<? super T> delegate;

    private NegateSpec(Spec<? super T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isSatisfiedBy(T element) {
        return !delegate.isSatisfiedBy(element);
    }

    public static <T> Spec<T> negate(Spec<? super T> spec) {
        return new NegateSpec<>(spec);
    }
}
