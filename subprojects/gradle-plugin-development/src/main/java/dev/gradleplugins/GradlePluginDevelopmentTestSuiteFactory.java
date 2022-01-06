package dev.gradleplugins;

import org.gradle.api.Project;

/**
 * Factory to create {@link GradlePluginDevelopmentTestSuite} instances.
 */
public interface GradlePluginDevelopmentTestSuiteFactory {
    /**
     * Creates {@code GradlePluginDevelopmentTestSuite} instance.
     *
     * @param name  test suite name, must not be null
     * @return a new {@link GradlePluginDevelopmentTestSuite} instance, never null
     */
    GradlePluginDevelopmentTestSuite create(String name);

    /**
     * Creates test suite factory for specified project.
     *
     * @param project  base Gradle project for factory, must not be null
     * @return a test suite factory, never null
     */
    static GradlePluginDevelopmentTestSuiteFactory forProject(Project project) {
        return new DefaultGradlePluginDevelopmentTestSuiteFactory(project.getObjects());
    }
}
