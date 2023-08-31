package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.Publication;

/**
 * Represent the publication of a Gradle plugin.
 *
 * @param <T> the publication type
 */
public interface GradlePluginDevelopmentPublication<T extends Publication> extends Named {
    DomainObjectSet<T> getAllPublications();

    DomainObjectSet<T> getPluginMarkerPublications();

    Provider<T> getPluginPublication();

    void pluginPublication(Action<? super T> configureAction);

    Class<T> getPublicationType();

    Property<Boolean> getExcluded();

    Property<String> getGroup();

    Property<String> getVersion();

    Property<String> getStatus();
}
