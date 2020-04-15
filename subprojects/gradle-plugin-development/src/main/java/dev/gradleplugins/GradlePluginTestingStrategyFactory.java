package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyInternal;

import java.util.Collections;

public abstract class GradlePluginTestingStrategyFactory {
    /**
     * Returns a testing strategy which will cover only the minimum Gradle version supported by the plugin.
     * It is possible to include the latest nightly using {@link NightlyAwareGradlePluginTestingStrategyBuilder#includeLatestNightlyVersion()}
     *
     * @return a {@link NightlyAwareGradlePluginTestingStrategyBuilder} instance for only the minimum Gradle version supported by the plugin.
     */
    public static NightlyAwareGradlePluginTestingStrategyBuilder onlyMinimumVersion() {
        return new GradlePluginTestingStrategyInternal.NightlyAwareGradlePluginTestingStrategyBuilderInternal(Collections.singleton("minimum"));
    }

    /**
     * Returns a testing strategy which will cover the latest minor Gradle version for each major Gradle version.
     * This strategy relies on https://services.gradle.org/versions/all data.
     * It is possible to include the latest nightly using {@link NightlyAwareGradlePluginTestingStrategyBuilder#includeLatestNightlyVersion()}
     *
     * @return a {@link NightlyAwareGradlePluginTestingStrategyBuilder} instance for all minor Gradle version for each major Gradle version.
     */
    public static NightlyAwareGradlePluginTestingStrategyBuilder latestMinorVersions() {
        return new GradlePluginTestingStrategyInternal.NightlyAwareGradlePluginTestingStrategyBuilderInternal(Collections.singleton("latestMinor"));
    }

    /**
     * Returns a testing strategy which will cover all released versions since the minimum supported Gradle version.
     * This strategy relies on https://services.gradle.org/versions/all data.
     * It is possible to include the latest nightly using {@link NightlyAwareGradlePluginTestingStrategyBuilder#includeLatestNightlyVersion()}
     *
     * @return a {@link NightlyAwareGradlePluginTestingStrategyBuilder} instance for all released Gradle version.
     */
    public static NightlyAwareGradlePluginTestingStrategyBuilder allReleasedVersions() {
        return new GradlePluginTestingStrategyInternal.NightlyAwareGradlePluginTestingStrategyBuilderInternal(Collections.singleton("all"));
    }

    /**
     * Returns a testing strategy which will cover only the latest Gradle nightly version.
     * This strategy relies on https://services.gradle.org/versions/all data.
     *
     * @return a {@link GradlePluginTestingStrategy} instance for the latest Gradle nightly version.
     */
    public static GradlePluginTestingStrategy latestNightlyVersion() {
        return new GradlePluginTestingStrategyInternal(Collections.singleton("latestNightly"));
    }

    /**
     * Returns a testing strategy which will cover only the currently running Gradle version.
     *
     * @return a {@link GradlePluginTestingStrategy} instance for the currently running Gradle version.
     */
    public static GradlePluginTestingStrategy none() {
        return new GradlePluginTestingStrategyInternal(Collections.singleton("default"));
    }

    /**
     * A builder class to further customized the testing strategy to include the latest nightly version.
     */
    public interface NightlyAwareGradlePluginTestingStrategyBuilder {

        /**
         * Returns a testing strategy which will cover the latest nightly Gradle version on top of the coverage already defined.
         * This strategy relies on https://services.gradle.org/versions/all data.
         *
         * @return a {@link GradlePluginTestingStrategy} instance for the latest nightly Gradle version together with coverage already defined.
         */
        GradlePluginTestingStrategy includeLatestNightlyVersion();
    }
}
