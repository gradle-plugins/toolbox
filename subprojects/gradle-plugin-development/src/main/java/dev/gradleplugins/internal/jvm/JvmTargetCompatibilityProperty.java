package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;

import java.util.Objects;

public final class JvmTargetCompatibilityProperty implements JvmCompatibilityProperty {
    private final JvmCompatibilityProperty delegate;
    private boolean finalized = false;

    public JvmTargetCompatibilityProperty(JvmCompatibilityProperty delegate) {
        this.delegate = delegate;
    }

    @Override
    public JavaVersion get() {
        return delegate.get();
    }

    @Override
    public void set(JavaVersion value) {
        Objects.requireNonNull(value);
        if (finalized) {
            throw new IllegalStateException("The value for property 'targetCompatibility' is final and cannot be changed any further.");
        }
        delegate.set(value);
    }

    @Override
    public void finalizeValue() {
        if (!finalized) {
            delegate.finalizeValue();
            finalized = true;
        }
    }
}