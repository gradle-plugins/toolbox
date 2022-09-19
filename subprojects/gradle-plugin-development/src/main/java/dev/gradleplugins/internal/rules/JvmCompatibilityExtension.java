package dev.gradleplugins.internal.rules;

import org.gradle.api.JavaVersion;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.util.Objects;

public interface JvmCompatibilityExtension {
    Property<JavaVersion> getSourceCompatibility();
    Property<JavaVersion> getTargetCompatibility();

    static JvmCompatibilityExtension jvm(GradlePluginDevelopmentExtension extension) {
        Objects.requireNonNull(extension);
        return ((ExtensionAware) extension).getExtensions().getByType(JvmCompatibilityExtension.class);
    }
}
