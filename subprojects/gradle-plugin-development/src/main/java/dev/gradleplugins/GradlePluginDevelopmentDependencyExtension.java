package dev.gradleplugins;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

/**
 * Extension methods for {@link DependencyHandler}.
 */
public interface GradlePluginDevelopmentDependencyExtension {
    /**
     * Returns the versioned redistributed Gradle API, that is {@literal dev.gradleplugins:gradle-api:$version}, external dependency.
     * @param version the version of the Gradle API.
     * @return a dependency instance for the specified version of the Gradle API.
     */
    Dependency gradleApi(String version);

    /**
     * Returns the Gradle Fixtures, that is {@literal dev.gradleplugins:gradle-fixtures:latest.release}, external dependency.
     * @return a dependency instance for the latest Gradle Fixtures.
     */
    Dependency gradleFixtures();
}
