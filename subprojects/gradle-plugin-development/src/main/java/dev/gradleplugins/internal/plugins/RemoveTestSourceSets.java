package dev.gradleplugins.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Project;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

final class RemoveTestSourceSets implements Action<Project> {
    @Override
    public void execute(Project project) {
        gradlePlugin(project).testSourceSets();
    }
}
