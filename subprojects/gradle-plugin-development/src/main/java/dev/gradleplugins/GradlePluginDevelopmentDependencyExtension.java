package dev.gradleplugins;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

/**
 * Extension methods for {@link DependencyHandler}.
 */
public interface GradlePluginDevelopmentDependencyExtension {
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
}
