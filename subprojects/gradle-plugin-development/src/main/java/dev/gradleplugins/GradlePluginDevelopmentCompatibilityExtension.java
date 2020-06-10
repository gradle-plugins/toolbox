package dev.gradleplugins;

import org.gradle.api.provider.Property;

/**
 * An extension for controlling the Gradle plugin development compatibility.
 * The extension is registered on the {@literal gradlePlugin} extension.
 */
public interface GradlePluginDevelopmentCompatibilityExtension {
    /**
     * Configures the minimum Gradle version.
     *
     * @return a property to configure the minimum support Gradle version, never null.
     */
    Property<String> getMinimumGradleVersion();
}
