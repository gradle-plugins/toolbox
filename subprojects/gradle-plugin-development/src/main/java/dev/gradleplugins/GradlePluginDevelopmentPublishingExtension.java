package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.publish.Publication;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.util.Objects;

/**
 * An extension for controlling the Gradle plugin development publishing.
 * The extension is registered on the {@literal gradlePlugin} extension.
 */
public interface GradlePluginDevelopmentPublishingExtension {
    /**
     * Register publication for Gradle plugin.
     *
     * @param name  the publication name
     * @param type  the publication type
     * @param <T>  the publication type
     */
    <T extends Publication> void registerPublication(String name, Class<T> type);

    /**
     * Register publication for Gradle plugin.
     *
     * @param name  the publication name
     * @param type  the publication type
     * @param configureAction  the configuration action
     * @param <T>  the publication type
     */
    <T extends Publication> void registerPublication(String name, Class<T> type, Action<? super GradlePluginDevelopmentPublication<T>> configureAction);

    /**
     * Returns all publications of this component.
     *
     * @return the publications for this component, never null.
     */
    NamedDomainObjectSet<GradlePluginDevelopmentPublication<?>> getPublications();

    /**
     * Configures all publication of this component.
     *
     * @param configureAction  the configure action
     */
    void publications(Action<? super NamedDomainObjectSet<GradlePluginDevelopmentPublication<?>>> configureAction);

    /**
     * Returns {@literal publishing} extension from Gradle plugin development extension.
     * The plugin {@literal dev.gradleplugins.gradle-plugin-base} registers this extension.
     *
     * @param extension  the {@literal gradlePlugin} extension, must not be null
     * @return the publishing extension, never null
     */
    static GradlePluginDevelopmentPublishingExtension publishing(GradlePluginDevelopmentExtension extension) {
        Objects.requireNonNull(extension);
        return (GradlePluginDevelopmentPublishingExtension) ((ExtensionAware) extension).getExtensions().getByName("publishing");
    }
}
