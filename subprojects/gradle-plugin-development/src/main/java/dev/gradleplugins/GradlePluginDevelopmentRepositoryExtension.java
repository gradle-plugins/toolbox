package dev.gradleplugins;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

/**
 * Extension methods for {@link RepositoryHandler}.
 */
public interface GradlePluginDevelopmentRepositoryExtension {
    /**
     * Adds the Gradle Plugin Development repository containing the Gradle API and fixtures.
     * This repository is enough for compiling against any versioned Gradle API.
     * The Gradle fixtures may require more repositories to be configured.
     *
     * @return the Gradle Plugin Development repository instance added to the repository handler.
     */
    MavenArtifactRepository gradlePluginDevelopment();
}
