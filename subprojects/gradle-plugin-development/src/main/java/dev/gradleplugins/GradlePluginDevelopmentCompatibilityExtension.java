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
     * Configures the Gradle API version to compile against.
     * Defaults to minimum Gradle version for non snapshot versions and {@literal local} for snapshot version.
     * Use {@literal local} to use the Gradle API JAR generated for the current distribution.
     *
     * <b>WARNING:</b> We don't recommend using the local Gradle API version as it lack support for source configuration and Groovy/Kotlin version alignment.
     *
     * @return a property to configure the Gradle API version to use, never null
     */
    Property<String> getGradleApiVersion();
}
