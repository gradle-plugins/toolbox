package dev.gradleplugins;

import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

/**
 * Groovy language extension for Gradle plugin development.
 */
public interface GroovyGradlePluginDevelopmentExtension {
    void withSourcesJar();
    void withJavadocJar();
    void withGroovydocJar();
    Property<String> getGroovyVersion();
    void disableDefaultRepositories();

    static GroovyGradlePluginDevelopmentExtension groovy(GradlePluginDevelopmentExtension extension) {
        return (GroovyGradlePluginDevelopmentExtension) ((ExtensionAware) extension).getExtensions().getByName("groovy");
    }
}
