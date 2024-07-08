package dev.gradleplugins.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class GradlePluginDevelopmentExtensionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("gradlepluginsdev.rules.project-repositories-extension");
        project.getPluginManager().apply("gradlepluginsdev.rules.project-dependencies-extension");
    }
}
