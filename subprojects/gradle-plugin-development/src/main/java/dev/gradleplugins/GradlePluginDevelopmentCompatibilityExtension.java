package dev.gradleplugins;

import org.gradle.api.provider.Property;

public interface GradlePluginDevelopmentCompatibilityExtension {
    Property<String> getMinimumGradleVersion();
}
