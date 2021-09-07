package dev.gradleplugins;

public interface GradleVersionCoverageTestingStrategy extends GradlePluginTestingStrategy {
    /**
     * Returns the Gradle version this testing strategy covers.
     *
     * @return Gradle version coverage, never null
     */
    String getVersion();

    /**
     * Returns {@literal true} if the Gradle version of this testing strategy is the latest Global Available (GA) according to <a href="https://services.gradle.org/versions/current">current version metadata</a>.
     *
     * @return {@literal true} if this testing strategy is for the latest GA version
     */
    boolean isLatestGlobalAvailable();

    /**
     * Returns {@literal true} if the Gradle version of this testing strategy is the latest Global Available (GA) according to <a href="https://services.gradle.org/versions/nightly">latest nightly version metadata</a>.
     *
     * @return {@literal true} if this testing strategy is for the latest nightly version
     */
    boolean isLatestNightly();
}
