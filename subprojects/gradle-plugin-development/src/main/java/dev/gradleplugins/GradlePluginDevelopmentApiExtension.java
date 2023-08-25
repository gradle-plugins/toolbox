package dev.gradleplugins;

import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.util.Objects;

/**
 * An extension for controlling the Gradle plugin development API.
 * The extension is registered on the {@literal gradlePlugin} extension.
 * The API refers to what a user can build against, e.g. some classes are for internal use only.
 * This extension offers some hook points to accomplish the separation at the source set or Jar task level.
 */
public interface GradlePluginDevelopmentApiExtension {
    /**
     * Configures the API source set for the plugin.
     *
     * <p>
     * <code>
     *     // Configure Gradle plugin API as a custom source set
     *     gradlePlugin {
     *         api {
     *             sourceSet = sourceSets.register('api')
     *         }
     *     }
     * </code>
     * </p>
     *
     * @return a property to configure the API source set, never null.
     */
    Property<SourceSet> getSourceSet();

    /**
     * Configures the API Jar for the plugin.
     *
     * <p>
     * <code>
     *     // Remove `api` packages from implementation JAR
     *     tasks.named('jar', Jar) {
     *         exclude('**\/api\/**')
     *     }
     *
     *     // Configure Gradle plugin API as a custom JAR that only include `api` packages
     *     gradlePlugin {
     *         api {
     *             jarTask = tasks.register('apiJar', Jar) {
     *                 from(sourceSet.flatMap { it.output.elements })
     *                 include('**\/api\/**')
     *                 archiveClassifier = 'api'
     *             }
     *         }
     *     }
     * </code>
     * </p>
     *
     * @return a property to configure the exported API JAR, never null.
     */
    Property<Jar> getJarTask();

    /**
     * Returns {@literal api} extension from Gradle plugin development extension.
     * The plugin {@literal dev.gradleplugins.gradle-plugin-base} registers this extension.
     *
     * @param extension  the {@literal gradlePlugin} extension, must not be null
     * @return the compatibility extension, never null
     */
    static GradlePluginDevelopmentApiExtension api(GradlePluginDevelopmentExtension extension) {
        Objects.requireNonNull(extension);
        return (GradlePluginDevelopmentApiExtension) ((ExtensionAware) extension).getExtensions().getByName("api");
    }
}
