package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import dev.gradleplugins.internal.GradlePluginDevelopmentRepositoryExtensionInternal;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

public abstract class GradlePluginDevelopmentExtensionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        new RegisterGradlePluginDevelopmentRepositoryExtensionRule().execute(project);
        new RegisterGradlePluginDevelopmentDependencyExtensionRule().execute(project);
        applyToRepositories(project.getRepositories());
        applyToDependencies(project.getDependencies(), project);
    }

    private void applyToRepositories(RepositoryHandler repositories) {
        val extension = new GradlePluginDevelopmentRepositoryExtensionInternal(repositories, GradlePluginDevelopmentRepositoryExtension.from(repositories));
        extension.applyTo(repositories);
    }

    private void applyToDependencies(DependencyHandler dependencies, Project project) {
        val extension = new GradlePluginDevelopmentDependencyExtensionInternal(dependencies, project, GradlePluginDevelopmentDependencyExtension.from(dependencies));
        extension.applyTo(dependencies);
    }
}
