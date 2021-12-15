package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;

public abstract class GradlePluginDevelopmentExtensionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        new RegisterGradlePluginDevelopmentRepositoryExtensionRule().execute(project);
        new RegisterGradlePluginDevelopmentDependencyExtensionRule().execute(project);
        applyToDependencies(project.getDependencies(), project);
    }

    private void applyToDependencies(DependencyHandler dependencies, Project project) {
        val extension = new GradlePluginDevelopmentDependencyExtensionInternal(dependencies, project, GradlePluginDevelopmentDependencyExtension.from(dependencies));
        extension.applyTo(dependencies);
    }
}
