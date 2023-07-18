package dev.gradleplugins;

import dev.gradleplugins.internal.runtime.dsl.DslMethod;
import dev.gradleplugins.internal.runtime.dsl.DslTarget;
import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.ExtensionAware;

/**
 * Extension methods for {@link RepositoryHandler}.
 */
@DslTarget(RepositoryHandler.class)
public interface GradlePluginDevelopmentRepositoryExtension {
    /**
     * Adds the Gradle Plugin Development repository containing the Gradle API and fixtures.
     * This repository is enough for compiling against any versioned Gradle API.
     * The Gradle fixtures may require more repositories to be configured.
     *
     * @return the Gradle Plugin Development repository instance added to the repository handler.
     */
    @DslMethod
    MavenArtifactRepository gradlePluginDevelopment();

    /**
     * Adds the Gradle Plugin Development repository containing the Gradle API and fixtures configured using the specified action.
     * This repository is enough for compiling against any versioned Gradle API.
     * The Gradle fixtures may require more repositories to be configured.
     *
     * @param action  the configure action for the repository
     * @return the Gradle Plugin Development repository instance added to the repository handler.
     */
    @DslMethod
    MavenArtifactRepository gradlePluginDevelopment(Action<? super MavenArtifactRepository> action);

    /**
     * Returns {@link RepositoryHandler} extension methods.
     *
     * @param repositories  the repositories to extends, must not be null
     * @return the extension methods, never null
     */
    static GradlePluginDevelopmentRepositoryExtension from(RepositoryHandler repositories) {
        return ((ExtensionAware) repositories).getExtensions().getByType(GradlePluginDevelopmentRepositoryExtension.class);
    }
}
