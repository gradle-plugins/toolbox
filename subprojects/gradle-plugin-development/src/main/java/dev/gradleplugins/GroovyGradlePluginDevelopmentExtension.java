package dev.gradleplugins;

import org.gradle.api.provider.Property;

public interface GroovyGradlePluginDevelopmentExtension {
    Property<String> getMinimumGradleVersion();
}
