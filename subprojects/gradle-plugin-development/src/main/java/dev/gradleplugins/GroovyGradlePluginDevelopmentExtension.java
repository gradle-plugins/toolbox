package dev.gradleplugins;

/**
 * Groovy language extension for Gradle plugin development.
 */
public interface GroovyGradlePluginDevelopmentExtension {
    void withSourcesJar();
    void withJavadocJar();
    void withGroovydocJar();
}
