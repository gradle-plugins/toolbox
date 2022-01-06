package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;
import org.gradle.api.plugins.JavaPluginExtension;

public class JvmCompatibilityPropertyFactory {
    public static JvmCompatibilityProperties of(JavaPluginExtension java) {
        return new JvmCompatibilityProperties(ofSourceCompatibility(java), ofTargetCompatibility(java));
    }

    public static JvmSourceCompatibilityProperty ofSourceCompatibility(JavaPluginExtension java) {
        return new JvmSourceCompatibilityProperty(new FinalizableJvmCompatibilityPropertyAdapter(new JvmCompatibilityProperty() {
            @Override
            public JavaVersion get() {
                return java.getSourceCompatibility();
            }

            @Override
            public void set(JavaVersion value) {
                java.setSourceCompatibility(value);
            }

            @Override
            public void finalizeValue() {
                // do nothing
            }
        }));
    }

    public static JvmTargetCompatibilityProperty ofTargetCompatibility(JavaPluginExtension java) {
        return new JvmTargetCompatibilityProperty(new FinalizableJvmCompatibilityPropertyAdapter(new JvmCompatibilityProperty() {
            @Override
            public JavaVersion get() {
                return java.getTargetCompatibility();
            }

            @Override
            public void set(JavaVersion value) {
                java.setTargetCompatibility(value);
            }

            @Override
            public void finalizeValue() {
                // do nothing
            }
        }));
    }
}
