package dev.gradleplugins.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

abstract /*final*/ class GradlePluginDevelopmentBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentBasePlugin() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java-gradle-plugin", new RegisterCompatibilityExtension(project));
        project.getPluginManager().withPlugin("java-gradle-plugin", new RemoveGradleApiProjectDependency(project));
    }
}
