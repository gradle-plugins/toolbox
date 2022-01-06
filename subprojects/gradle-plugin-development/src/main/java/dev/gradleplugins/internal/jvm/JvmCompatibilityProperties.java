package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;

public final class JvmCompatibilityProperties {
    private final JvmTargetCompatibilityProperty targetCompatibility;
    private final JvmSourceCompatibilityProperty sourceCompatibility;

    public JvmCompatibilityProperties(JvmTargetCompatibilityProperty targetCompatibility, JvmSourceCompatibilityProperty sourceCompatibility) {
        this.targetCompatibility = targetCompatibility;
        this.sourceCompatibility = sourceCompatibility;
    }

    public void set(JavaVersion value) {
        targetCompatibility.set(value);
        sourceCompatibility.set(value);
    }

    public void finalizeValues() {
        targetCompatibility.finalizeValue();
        sourceCompatibility.finalizeValue();
    }
}
