package dev.gradleplugins.internal.util;

import groovy.lang.Closure;
import org.gradle.api.Action;

public interface Configurable<SELF> extends org.gradle.util.Configurable<SELF> {
    @Override
    @SuppressWarnings("unchecked")
    default SELF configure(@SuppressWarnings("rawtype") Closure cl) {
        configure(new ClosureWrappedConfigureAction<>(cl));
        return (SELF) this;
    }

    void configure(Action<? super SELF> action);
}
