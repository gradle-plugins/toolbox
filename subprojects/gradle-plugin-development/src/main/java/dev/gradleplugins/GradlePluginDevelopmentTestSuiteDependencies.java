package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;

public interface GradlePluginDevelopmentTestSuiteDependencies {
    void implementation(Object notation);

    void implementation(Object notation, Action<? super ModuleDependency> action);

    void compileOnly(Object notation);

    void annotationProcessor(Object notation);

    void pluginUnderTestMetadata(Object notation);

    Dependency testFixtures(Object notation);

    Dependency spockFramework();

    Dependency spockFramework(String version);

    Dependency gradleFixtures();

    Dependency gradleTestKit();
}
