package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;

final class MockJvmCompatibilityProperty implements JvmCompatibilityProperty {
    public JavaVersion value;

    public MockJvmCompatibilityProperty(JavaVersion defaultValue) {
        this.value = defaultValue;
    }

    @Override
    public JavaVersion get() {
        return value;
    }

    @Override
    public void set(JavaVersion value) {
        this.value = value;
    }

    @Override
    public void finalizeValue() {
        // do nothing
    }
}
