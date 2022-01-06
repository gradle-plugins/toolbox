package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;

public interface JvmCompatibilityProperty {
    JavaVersion get();
    void set(JavaVersion value);
    void finalizeValue();
}
