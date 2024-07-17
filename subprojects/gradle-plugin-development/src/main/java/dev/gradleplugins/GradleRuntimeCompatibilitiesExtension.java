package dev.gradleplugins;

import org.gradle.api.JavaVersion;
import org.gradle.api.provider.Provider;
import org.gradle.util.GradleVersion;

/**
 * Gradle extension to access Gradle runtime information such as packaged version of Groovy and Kotlin.
 * <p>
 * All method takes a Gradle version in either of the following forms:
 * <ul>
 *     <li>{@link GradleVersion}: a Gradle version instance</li>
 *     <li>String: any String parsable by {@link GradleVersion#version(String)}</li>
 *     <li>{@link Provider} to any supported types</li>
 *     <li>{@link Object#toString()} for any other instance</li>
 * </ul>
 */
public interface GradleRuntimeCompatibilitiesExtension {
    /**
     * Returns the Groovy version packaged with the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Groovy version of the specified Gradle version, never null.
     */
    Provider<String> groovyVersionOf(Object gradleVersion);

    /**
     * Returns the minimum Java version of the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the minimum Java version for the specified Gradle version, never null.
     */
    Provider<JavaVersion> minimumJavaVersionFor(Object gradleVersion);

    /**
     * Returns the Kotlin version packaged with the specified Gradle version.
     * All Gradle version without Kotlin DSL support will return an empty value.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Kotlin version for the specified Gradle version, never null.
     */
    Provider<String> kotlinVersionOf(Object gradleVersion);

    /**
     * Returns the last patched Gradle version for the specified Gradle version.
     * For example, passing Gradle version {@literal 6.2} would return {@literal 6.2.2}, the last patched version of {@literal 6.2.x}.
     *
     * @param gradleVersion  the Gradle version to find the last patched version, must not be null.
     * @return the latest patched version for the specified version, never null
     */
    Provider<GradleVersion> lastPatchedVersionOf(Object gradleVersion);

    /**
     * Returns the last minor released Gradle version for the specified major Gradle version.
     * For example, passing Gradle version {@literal 7.3} would return {@literal 7.6.4}, the last minor release of {@literal 7.x}.
     * The function will return the last patched version as well.
     *
     * @param gradleVersion  the Gradle version to find the last minor release for, must not be null
     * @return the latest minor version for the specified version, never null
     */
    Provider<GradleVersion> lastMinorReleaseOf(Object gradleVersion);
}
