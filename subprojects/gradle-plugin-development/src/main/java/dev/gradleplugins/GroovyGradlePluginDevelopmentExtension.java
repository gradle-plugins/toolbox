package dev.gradleplugins;

import org.gradle.api.provider.Property;

/**
 * Groovy language extension for Gradle plugin development.
 */
public interface GroovyGradlePluginDevelopmentExtension {
    void withSourcesJar();
    void withJavadocJar();
    void withGroovydocJar();
    Property<String> getGroovyVersion();
    void disableDefaultRepositories();
}
