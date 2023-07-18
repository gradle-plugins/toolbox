package dev.gradleplugins.internal.runtime.dsl;

public interface GradleExtensionMixInBuilder<T> {
    GradleExtensionMixInBuilder<T> useInstance(T instance);
    GradleExtensionMixIn build();
}
