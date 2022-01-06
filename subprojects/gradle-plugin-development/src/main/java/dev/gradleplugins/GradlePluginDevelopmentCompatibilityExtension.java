package dev.gradleplugins;

import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.util.Objects;

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

    /**
     * Returns {@literal compatibility} extension from Gradle plugin development extension.
     * The plugin {@literal dev.gradleplugins.base} registers this extension.
     *
     * @param extension  the {@literal gradlePlugin} extension, must not be null
     * @return the compatibility extension, never null
     */
    static GradlePluginDevelopmentCompatibilityExtension compatibility(GradlePluginDevelopmentExtension extension) {
        Objects.requireNonNull(extension);
        return (GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware) extension).getExtensions().getByName("compatibility");
    }
}
