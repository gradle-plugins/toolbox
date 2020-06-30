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

    /**
     * Configures the compatibility system to skip setting the minimum java version.
     *
     * There are cases where a higher minimum Java version is allowed, such as internal plugins
     * guaranteed to run on specific Java versions at minimum.
     *
     * @return a property to configure skipping minimum Java version, never null
     */
    Property<Boolean> getSkipMinimumJavaVersion();
}
