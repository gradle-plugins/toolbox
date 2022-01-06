package dev.gradleplugins.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class GradlePluginDevelopmentExtensionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        new RegisterGradlePluginDevelopmentRepositoryExtensionRule().execute(project);
        new RegisterGradlePluginDevelopmentDependencyExtensionRule().execute(project);
    }
}
