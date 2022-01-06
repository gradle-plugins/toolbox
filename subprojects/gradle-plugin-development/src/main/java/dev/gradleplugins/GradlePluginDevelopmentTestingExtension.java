package dev.gradleplugins;

import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.util.Objects;

/**
 * Extension for {@link org.gradle.plugin.devel.GradlePluginDevelopmentExtension}.
 */
public interface GradlePluginDevelopmentTestingExtension {
    /**
     * Registers new test suite of specified name.
     *
     * @param name  the test suite name, must not be null
     * @return a {@link GradlePluginDevelopmentTestSuite} instance, never null
     */
    GradlePluginDevelopmentTestSuite registerSuite(String name);

    /**
     * Returns {@literal testing} extension from Gradle plugin development extension.
     * The plugin {@literal dev.gradleplugins.gradle-plugin-testing-base} registers this extension.
     *
     * @param extension  the {@literal gradlePlugin} extension, must not be null
     * @return the testing extension, never null
     */
    static GradlePluginDevelopmentTestingExtension testing(GradlePluginDevelopmentExtension extension) {
        Objects.requireNonNull(extension);
        return (GradlePluginDevelopmentTestingExtension) ((ExtensionAware) extension).getExtensions().getByName("testing");
    }
}
