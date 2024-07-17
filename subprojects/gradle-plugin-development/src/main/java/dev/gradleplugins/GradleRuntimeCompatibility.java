package dev.gradleplugins;

import org.gradle.api.JavaVersion;

import java.util.Optional;

/**
 * Query Gradle runtime compatibility information for specific Gradle version.
 * It supports the Groovy and Kotlin version packaged for each Gradle version.
 * It also supports the minimum Java version supported by each Gradle version.
 */
@Deprecated // use gradleRuntimeCompatibilities extension
public final class GradleRuntimeCompatibility {
    /**
     * Returns the Groovy version packaged with the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Groovy version of the specified Gradle version, never null.
     */
    public static String groovyVersionOf(String gradleVersion) {
        return dev.gradleplugins.internal.util.GradleRuntimeCompatibility.groovyVersionOf(gradleVersion);
    }

    /**
     * Returns the Groovy version packaged with the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Groovy version of the specified Gradle version, never null.
     */
    @Deprecated
    public static String groovyVersionOf(org.gradle.util.VersionNumber gradleVersion) {
        return dev.gradleplugins.internal.util.GradleRuntimeCompatibility.groovyVersionOf(gradleVersion.toString());
    }

    /**
     * Returns the minimum Java version of the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the minimum Java version for the specified Gradle version, never null.
     */
    public static JavaVersion minimumJavaVersionFor(String gradleVersion) {
        return dev.gradleplugins.internal.util.GradleRuntimeCompatibility.minimumJavaVersionFor(gradleVersion);
    }

    /**
     * Returns the minimum Java version of the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the minimum Java version for the specified Gradle version, never null.
     */
    @Deprecated
    public static JavaVersion minimumJavaVersionFor(org.gradle.util.VersionNumber gradleVersion) {
        return dev.gradleplugins.internal.util.GradleRuntimeCompatibility.minimumJavaVersionFor(gradleVersion.toString());
    }

    /**
     * Returns the Kotlin version packaged with the specified Gradle version.
     * All Gradle version without Kotlin DSL support will return an empty value.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Kotlin version for the specified Gradle version, never null.
     */
    public static Optional<String> kotlinVersionOf(String gradleVersion) {
        return dev.gradleplugins.internal.util.GradleRuntimeCompatibility.kotlinVersionOf(gradleVersion);
    }

    /**
     * Returns the Kotlin version packaged with the specified Gradle version.
     * All Gradle version without Kotlin DSL support will return an empty value.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Kotlin version for the specified Gradle version, never null.
     */
    @Deprecated
    public static Optional<String> kotlinVersionOf(org.gradle.util.VersionNumber gradleVersion) {
        return dev.gradleplugins.internal.util.GradleRuntimeCompatibility.kotlinVersionOf(gradleVersion.toString());
    }

    /**
     * Returns the last patched Gradle version for the specified Gradle version.
     * For example, passing Gradle version {@literal 6.2} would return {@literal 6.2.2}, the last patched version of {@literal 6.2.x}.
     *
     * @param gradleVersion  the Gradle version to find the last patched version, must not be null.
     * @return the latest patched version for the specified version, never null
     */
    public static String lastPatchedVersionOf(String gradleVersion) {
        return dev.gradleplugins.internal.util.GradleRuntimeCompatibility.lastPatchedVersionOf(gradleVersion);
    }

    /**
     * Returns the last minor released Gradle version for the specified major Gradle version.
     * For example, passing Gradle version {@literal 7.3} would return {@literal 7.6}, the last minor release of {@literal 7.x}.
     * The function will return the last patched version as well.
     *
     * @param gradleVersion  the Gradle version to find the last minor release for, must not be null
     * @return the latest minor version for the specified version, never null
     */
    public static String lastMinorReleaseOf(String gradleVersion) {
        return dev.gradleplugins.internal.util.GradleRuntimeCompatibility.lastMinorReleaseOf(gradleVersion);
    }
}
