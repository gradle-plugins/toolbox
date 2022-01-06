package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;

public final class JvmCompatibilityProperties {
    private final JvmSourceCompatibilityProperty sourceCompatibility;
    private final JvmTargetCompatibilityProperty targetCompatibility;

    public JvmCompatibilityProperties(JvmSourceCompatibilityProperty sourceCompatibility, JvmTargetCompatibilityProperty targetCompatibility) {
        this.sourceCompatibility = sourceCompatibility;
        this.targetCompatibility = targetCompatibility;
    }

    public void set(JavaVersion value) {
        sourceCompatibility.set(value);
        targetCompatibility.set(value);
    }

    public void finalizeValues() {
        sourceCompatibility.finalizeValue();
        targetCompatibility.finalizeValue();
    }
}
