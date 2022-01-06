package dev.gradleplugins;

/**
 * Extension for {@link org.gradle.plugin.devel.GradlePluginDevelopmentExtension}.
 */
public interface GradlePluginDevelopmentTestingExtension {
    GradlePluginDevelopmentTestSuite registerSuite(String name);
}
