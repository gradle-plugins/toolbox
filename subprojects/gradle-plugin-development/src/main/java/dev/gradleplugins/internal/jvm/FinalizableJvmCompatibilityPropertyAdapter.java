package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;

import java.util.Objects;

public final class FinalizableJvmCompatibilityPropertyAdapter implements JvmCompatibilityProperty {
    private final JvmCompatibilityProperty delegate;
    private final JavaVersion initialValue;

    public FinalizableJvmCompatibilityPropertyAdapter(JvmCompatibilityProperty delegate) {
        this.delegate = delegate;
        this.initialValue = delegate.get();

        // The plugins assume no one will ever use this value
        delegate.set(JavaVersion.VERSION_1_1);
    }

    @Override
    public JavaVersion get() {
        return delegate.get();
    }

    @Override
    public void set(JavaVersion value) {
        Objects.requireNonNull(value);
        // Only set the value if we still have the magical default
        if (delegate.get().equals(JavaVersion.VERSION_1_1)) {
            delegate.set(value);
        }
    }

    @Override
    public void finalizeValue() {
        // Restore default values if needed
        if (delegate.get().equals(JavaVersion.VERSION_1_1)) {
            delegate.set(initialValue);
        }
    }
}
