package dev.gradleplugins;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.provider.Provider;

//@DslDecorator(DependencyBucketDecorator.class)
public interface GradlePluginDevelopmentTestSuiteDependencies extends GradlePluginDevelopmentDependencyModifiers.PlatformDependencyModifiers, GradlePluginDevelopmentDependencyModifiers.TestFixturesDependencyModifiers {
    GradlePluginDevelopmentDependencyBucket getImplementation();
    GradlePluginDevelopmentDependencyBucket getCompileOnly();
    GradlePluginDevelopmentDependencyBucket getRuntimeOnly();
    GradlePluginDevelopmentDependencyBucket getAnnotationProcessor();
    GradlePluginDevelopmentDependencyBucket getPluginUnderTestMetadata();

    @Deprecated
    Dependency spockFramework();

    @Deprecated
    Dependency spockFramework(String version);

    @Deprecated
    Dependency gradleFixtures();

    Dependency gradleTestKit();

    Dependency gradleTestKit(String version);

    @Deprecated
    Provider<Dependency> groovy();

    @Deprecated
    Dependency groovy(String version);

    Dependency gradleApi(String version);

    ProjectDependency project(String projectPath);
    ProjectDependency project();
}
