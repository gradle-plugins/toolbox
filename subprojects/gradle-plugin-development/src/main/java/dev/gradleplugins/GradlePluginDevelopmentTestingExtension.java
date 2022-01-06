package dev.gradleplugins;

/**
 * Extension for {@link org.gradle.plugin.devel.GradlePluginDevelopmentExtension}.
 */
public interface GradlePluginDevelopmentTestingExtension {
    /**
     * Registers new test suite of specified name.
     *
     * @param name  the test suite name, must not be null
     * @return a {@link GradlePluginDevelopmentTestSuite} instance, never null
     */
    GradlePluginDevelopmentTestSuite registerSuite(String name);
}
