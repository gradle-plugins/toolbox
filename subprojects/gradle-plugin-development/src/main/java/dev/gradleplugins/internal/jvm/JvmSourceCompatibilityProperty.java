package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;

import java.util.Objects;

public final class JvmSourceCompatibilityProperty implements JvmCompatibilityProperty {
    private final JvmCompatibilityProperty delegate;

    public JvmSourceCompatibilityProperty(JvmCompatibilityProperty delegate) {
        this.delegate = delegate;
    }

    @Override
    public JavaVersion get() {
        return delegate.get();
    }

    @Override
    public void set(JavaVersion value) {
        Objects.requireNonNull(value);
        delegate.set(value);
    }

    @Override
    public void finalizeValue() {
        delegate.finalizeValue();
    }
}
