package dev.gradleplugins;

import dev.gradleplugins.internal.runtime.dsl.DslMethod;
import dev.gradleplugins.internal.runtime.dsl.DslTarget;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.Objects;

/**
 * Extension methods for {@link DependencyHandler}.
 */
@DslTarget(DependencyHandler.class)
public interface GradlePluginDevelopmentDependencyExtension {
    String GRADLE_API_LOCAL_VERSION = "local";

    /**
     * Returns the versioned redistributed Gradle API, that is {@literal dev.gradleplugins:gradle-api:$version}, external dependency.
     *
     * @param version  the version of the Gradle API, must not be null
     * @return a dependency instance for the specified version of the Gradle API, never null
     */
    @DslMethod
    Dependency gradleApi(String version);

    /**
     * Returns the versioned redistributed Gradle Test Kit, that is {@literal dev.gradleplugins:gradle-test-kit:$version}, external dependency.
     *
     * @param version the version of the Gradle Test Kit, must not be null
     * @return a dependency instance for the specified version of the Gradle Test Kit, never null
     */
    @DslMethod
    Dependency gradleTestKit(String version);

    /**
     * Returns the Gradle Fixtures, that is {@literal dev.gradleplugins:gradle-fixtures:latest.release}, external dependency.
     *
     * @return a dependency instance for the latest Gradle Fixtures, never null
     */
    @DslMethod
    Dependency gradleFixtures();

    /**
     * Returns the Gradle Runner Kit with all supported executor, that is {@literal dev.gradleplugins:gradle-runner-kit:latest.release}, external dependency.
     *
     * @return a dependency instance for the latest Gradle Runner Kit with all supported executor, never null
     */
    @DslMethod
    Dependency gradleRunnerKit();

    /**
     * Returns the Gradle plugin's external dependency marked by the specified plugin notation.
     * The Gradle plugin marker consist of a published redirection artifact at {@literal <plugin-id>:<plugin-id>.gradle.plugin:<version>}.
     * The plugin notation is a short form notation a-la Maven: {@literal <plugin-id>:<version>}.
     *
     * @param pluginNotation  the plugin id and version of the Gradle plugin dependency
     * @return a dependency instance to the Gradle Plugin marked by the specified notation, never null
     */
    @DslMethod
    ExternalModuleDependency gradlePlugin(String pluginNotation);

    /**
     * Returns {@link DependencyHandler} extension methods.
     *
     * @param dependencies  the dependencies to extends, must not be null
     * @return the extension methods, never null
     */
    static GradlePluginDevelopmentDependencyExtension from(DependencyHandler dependencies) {
        Objects.requireNonNull(dependencies);
        return dependencies.getExtensions().getByType(GradlePluginDevelopmentDependencyExtension.class);
    }
}
