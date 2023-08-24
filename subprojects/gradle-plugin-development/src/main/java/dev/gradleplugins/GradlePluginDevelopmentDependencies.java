package dev.gradleplugins;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

//@DslDecorator(DependencyBucketDecorator.class)
public interface GradlePluginDevelopmentDependencies extends GradlePluginDevelopmentDependencyModifiers.PlatformDependencyModifiers {
    GradlePluginDevelopmentDependencyBucket getApi();
    GradlePluginDevelopmentDependencyBucket getImplementation();
    GradlePluginDevelopmentDependencyBucket getCompileOnly();
    GradlePluginDevelopmentDependencyBucket getCompileOnlyApi();
    GradlePluginDevelopmentDependencyBucket getRuntimeOnly();
    GradlePluginDevelopmentDependencyBucket getAnnotationProcessor();

    Dependency gradleApi(String version);

    ProjectDependency project(String projectPath);
    ProjectDependency project();

    ExternalModuleDependency gradlePlugin(String pluginNotation);

    static GradlePluginDevelopmentDependencies dependencies(GradlePluginDevelopmentExtension extension) {
        return (GradlePluginDevelopmentDependencies) ((ExtensionAware) extension).getExtensions().getByName("dependencies");
    }
}
