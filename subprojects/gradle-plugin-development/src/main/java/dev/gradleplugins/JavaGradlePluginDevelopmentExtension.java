package dev.gradleplugins;

/**
 * Java language extension for Gradle plugin development.
 */
public interface JavaGradlePluginDevelopmentExtension {
    void withSourcesJar();
    void withJavadocJar();
}
