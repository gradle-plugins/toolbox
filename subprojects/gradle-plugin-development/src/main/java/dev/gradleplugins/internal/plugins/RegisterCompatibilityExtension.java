package dev.gradleplugins.internal.plugins;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtensionAware;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;

final class RegisterCompatibilityExtension implements Action<Project> {
    private static final String EXTENSION_NAME = "compatibility";

    @Override
    public void execute(Project project) {
        val extension = newCompatibilityExtension(project);

        ((ExtensionAware) gradlePlugin(project)).getExtensions().add(EXTENSION_NAME, extension);

        project.afterEvaluate(finalize(extension));
    }

    private DefaultGradlePluginDevelopmentCompatibilityExtension newCompatibilityExtension(Project project) {
        return project.getObjects().newInstance(DefaultGradlePluginDevelopmentCompatibilityExtension.class, java(project));
    }

    private static Action<Project> finalize(DefaultGradlePluginDevelopmentCompatibilityExtension extension) {
        return ignored -> extension.finalizeComponent();
    }
}
