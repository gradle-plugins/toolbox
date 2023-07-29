package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public final class RemoveTestSourceSetsRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        assert project.getPluginManager().hasPlugin("java-gradle-plugin");
        gradlePlugin(project, developmentExtension -> developmentExtension.testSourceSets());
    }

    private static void gradlePlugin(Project project, Action<? super GradlePluginDevelopmentExtension> configureAction) {
        configureAction.execute((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin"));
    }
}
