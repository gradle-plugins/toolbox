package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;

public interface GradlePluginDevelopmentTestSuiteDependencies extends GradlePluginDevelopmentDependencyModifiers.PlatformDependencyModifiers, GradlePluginDevelopmentDependencyModifiers.TestFixturesDependencyModifiers {
    // TODO(2.0): Remove this method
    // Essentially deprecated, use getImplementation().add(...)
    void implementation(Object notation);

    // TODO(2.0): Remove this method
    // Essentially deprecated, use getImplementation().add(...)
    void implementation(Object notation, Action<? super ModuleDependency> action);

    GradlePluginDevelopmentDependencyBucket getImplementation();

    // TODO(2.0): Remove this method
    // Essentially deprecated, use getCompileOnly().add(...)
    void compileOnly(Object notation);

    GradlePluginDevelopmentDependencyBucket getCompileOnly();

    // TODO(2.0): Remove this method
    // Essentially deprecated, use getRuntimeOnly().add(...)
    void runtimeOnly(Object notation);

    GradlePluginDevelopmentDependencyBucket getRuntimeOnly();

    // TODO(2.0): Remove this method
    // Essentially deprecated, use getAnnotationProcessor().add(...)
    void annotationProcessor(Object notation);

    GradlePluginDevelopmentDependencyBucket getAnnotationProcessor();

    // TODO(2.0): Remove this method
    // Essentially deprecated, use getPluginUnderTest().add(...)
    void pluginUnderTestMetadata(Object notation);

    // TODO(2.0): Remove this method
    @Deprecated
    NamedDomainObjectProvider<Configuration> getPluginUnderTestMetadata();

    GradlePluginDevelopmentDependencyBucket getPluginUnderTest();

    // TODO(2.0): Remove this method
    // Essentially deprecated, use getTestFixtures().modify(...)
    ModuleDependency testFixtures(Object notation);

    // TODO(2.0): Remove this method
    // Essentially deprecated, use getPlatform().modify(...)
    ModuleDependency platform(Object notation);

    ProjectDependency project(String projectPath);
    ProjectDependency project();

    // TODO(2.0): Remove this method
    @Deprecated
    Object spockFramework();

    // TODO(2.0): Remove this method
    @Deprecated
    Object spockFramework(String version);

    // TODO(2.0): Remove this method
    @Deprecated
    Object gradleFixtures();

    // TODO(2.0): Return SelfResolvingDependency
    Object gradleTestKit();

    // TODO(2.0): Return Dependency
    Object gradleTestKit(String version);

    // TODO(2.0): Remove this method
    @Deprecated
    Object groovy();

    // TODO(2.0): Remove this method
    @Deprecated
    Object groovy(String version);

    // TODO(2.0): Return Dependency
    Object gradleApi(String version);
}
