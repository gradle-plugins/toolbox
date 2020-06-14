package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import dev.gradleplugins.internal.GradlePluginDevelopmentRepositoryExtensionInternal;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

public abstract class GradlePluginDevelopmentExtensionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        applyToRepositories(project.getRepositories());
        applyToDependencies(project.getDependencies());
    }

    private void applyToRepositories(RepositoryHandler repositories) {
        GradlePluginDevelopmentRepositoryExtensionInternal extension = new GradlePluginDevelopmentRepositoryExtensionInternal(repositories);
        extension.applyTo(repositories);
    }

    private void applyToDependencies(DependencyHandler dependencies) {
        GradlePluginDevelopmentDependencyExtensionInternal extension = new GradlePluginDevelopmentDependencyExtensionInternal(dependencies);
        extension.applyTo(dependencies);
    }
}
