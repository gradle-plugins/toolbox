package dev.gradleplugins;

import org.gradle.api.provider.Provider;
import org.gradle.util.GradleVersion;

import java.util.Set;

public interface GradlePluginTestingStrategyFactory {
    /**
     * Returns a testing strategy which will cover the minimum Gradle version supported by the plugin.
     * This strategy relies on the {@link GradlePluginDevelopmentCompatibilityExtension#getMinimumGradleVersion()}
     *
     * @return a {@link GradlePluginTestingStrategy} instance for the minimum Gradle version supported by the plugin, never null
     */
    GradleVersionCoverageTestingStrategy getCoverageForMinimumVersion();

    /**
     * Returns a testing strategy which will cover the latest Gradle nightly version.
     * This strategy relies on https://services.gradle.org/versions/nightly data.
     *
     * @return a {@link GradlePluginTestingStrategy} instance for the latest Gradle nightly version, never null
     */
    GradleVersionCoverageTestingStrategy getCoverageForLatestNightlyVersion();

    /**
     * Returns a testing strategy which will cover the latest Gradle released version of each major version above the minimum supported version.
     * This strategy imply {@link #getCoverageForLatestGlobalAvailableVersion()}.
     * This strategy may result in multiple {@link GradleVersionCoverageTestingStrategy}.
     *
     * @return a set of {@link GradlePluginTestingStrategy} instance for the latest Gradle GA version of each major version above minimum supported version, never null
     */
    Provider<Set<GradleVersionCoverageTestingStrategy>> getCoverageForLatestGlobalAvailableVersionOfEachSupportedMajorVersions();

    /**
     * Returns a testing strategy which will cover the latest Gradle released version.
     * This strategy relies on https://services.gradle.org/versions/current data.
     *
     * @return a {@link GradlePluginTestingStrategy} instance for the latest Gradle GA version, never null
     */
    GradleVersionCoverageTestingStrategy getCoverageForLatestGlobalAvailableVersion();

    /**
     * Returns a testing strategy which will cover the specified Gradle version.
     *
     * @return a {@link GradlePluginTestingStrategy} instance for the specified Gradle version, never null
     */
    GradleVersionCoverageTestingStrategy coverageForGradleVersion(String version);
}
