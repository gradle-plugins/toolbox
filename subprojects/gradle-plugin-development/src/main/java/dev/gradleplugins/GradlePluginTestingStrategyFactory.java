package dev.gradleplugins;

public interface GradlePluginTestingStrategyFactory {
    /**
     * Returns a testing strategy which will cover the minimum Gradle version supported by the plugin.
     * This strategy relies on the {@link GradlePluginDevelopmentCompatibilityExtension#getMinimumGradleVersion()}
     *
     * @return a {@link GradlePluginTestingStrategy} instance for the minimum Gradle version supported by the plugin.
     */
    GradlePluginTestingStrategy getCoverageForMinimumVersion();

    /**
     * Returns a testing strategy which will cover the latest Gradle nightly version.
     * This strategy relies on https://services.gradle.org/versions/nightly data.
     *
     * @return a {@link GradlePluginTestingStrategy} instance for the latest Gradle nightly version.
     */
    GradlePluginTestingStrategy getCoverageForLatestNightlyVersion();

    /**
     * Returns a testing strategy which will cover the latest Gradle released version.
     * This strategy relies on https://services.gradle.org/versions/current data.
     *
     * @return a {@link GradlePluginTestingStrategy} instance for the latest Gradle nightly version.
     */
    GradlePluginTestingStrategy getCoverageForLatestGlobalAvailableVersion();
}
