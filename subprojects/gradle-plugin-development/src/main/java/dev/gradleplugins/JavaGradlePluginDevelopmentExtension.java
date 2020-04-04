package dev.gradleplugins;

import org.gradle.api.provider.Property;

public interface JavaGradlePluginDevelopmentExtension {
    Property<String> getMinimumGradleVersion();
    void withSourcesJar();
    void withJavadocJar();
}
