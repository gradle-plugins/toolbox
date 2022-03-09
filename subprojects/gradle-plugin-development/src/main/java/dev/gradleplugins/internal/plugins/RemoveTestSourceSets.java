package dev.gradleplugins.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

final class RemoveTestSourceSets implements Action<AppliedPlugin> {
    private final Project project;

    RemoveTestSourceSets(Project project) {
        this.project = project;
    }

    @Override
    public void execute(AppliedPlugin ignored) {
        gradlePlugin(project).testSourceSets();
    }
}
