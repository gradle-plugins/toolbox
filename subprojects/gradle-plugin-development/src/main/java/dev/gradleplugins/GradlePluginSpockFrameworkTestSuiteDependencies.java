package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;

public interface GradlePluginSpockFrameworkTestSuiteDependencies {
    void implementation(Object notation);

    void implementation(Object notation, Action<? super ExternalModuleDependency> action);

    void compileOnly(Object notation);

    void annotationProcessor(Object notation);

    void pluginUnderTestMetadata(Object notation);
}
