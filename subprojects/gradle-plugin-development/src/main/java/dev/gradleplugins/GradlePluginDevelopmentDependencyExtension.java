package dev.gradleplugins;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.Objects;

/**
 * Extension methods for {@link DependencyHandler}.
 */
public interface GradlePluginDevelopmentDependencyExtension {
    String GRADLE_API_LOCAL_VERSION = "local";

    /**
     * Returns the versioned redistributed Gradle API, that is {@literal dev.gradleplugins:gradle-api:$version}, external dependency.
     *
     * @param version  the version of the Gradle API, must not be null
     * @return a dependency instance for the specified version of the Gradle API, never null
     */
    Dependency gradleApi(String version);

    /**
     * Returns the versioned redistributed Gradle Test Kit, that is {@literal dev.gradleplugins:gradle-test-kit:$version}, external dependency.
     *
     * @param version the version of the Gradle Test Kit, must not be null
     * @return a dependency instance for the specified version of the Gradle Test Kit, never null
     */
    Dependency gradleTestKit(String version);

    /**
     * Returns the Gradle Fixtures, that is {@literal dev.gradleplugins:gradle-fixtures:latest.release}, external dependency.
     *
     * @return a dependency instance for the latest Gradle Fixtures, never null
     */
    Dependency gradleFixtures();

    /**
     * Returns the Gradle Runner Kit with all supported executor, that is {@literal dev.gradleplugins:gradle-runner-kit:latest.release}, external dependency.
     *
     * @return a dependency instance for the latest Gradle Runner Kit with all supported executor, never null
     */
    Dependency gradleRunnerKit();

    /**
     * Returns {@link DependencyHandler} extension methods.
     *
     * @param dependencies  the dependencies to extends, must not be null
     * @return the extension methods, never null
     */
    static GradlePluginDevelopmentDependencyExtension from(DependencyHandler dependencies) {
        Objects.requireNonNull(dependencies);
        return new DefaultGradlePluginDevelopmentDependencyExtension(dependencies);
    }
}
