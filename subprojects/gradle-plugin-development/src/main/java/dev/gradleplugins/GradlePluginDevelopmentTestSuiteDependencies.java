package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

public interface GradlePluginDevelopmentTestSuiteDependencies {
    void implementation(Object notation);

    void implementation(Object notation, Action<? super ModuleDependency> action);

    void compileOnly(Object notation);

    void runtimeOnly(Object notation);

    void annotationProcessor(Object notation);

    void pluginUnderTestMetadata(Object notation);

    Object testFixtures(Object notation);

    Object platform(Object notation);

    @Deprecated
    Object spockFramework();

    @Deprecated
    Object spockFramework(String version);

    Object gradleFixtures();

    Object gradleTestKit();

    @Deprecated
    Object groovy();

    @Deprecated
    Object groovy(String version);

    Object gradleApi(String version);
}
