package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtensionAware;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

final class RegisterCompatibilityExtension implements Action<AppliedPlugin> {
    private static final String EXTENSION_NAME = "compatibility";
    private final Project project;

    RegisterCompatibilityExtension(Project project) {
        this.project = project;
    }

    @Override
    public void execute(AppliedPlugin ignored) {
        GradlePluginDevelopmentCompatibilityExtension extension = project.getObjects().newInstance(GradlePluginDevelopmentCompatibilityExtension.class);

        ((ExtensionAware) gradlePlugin(project)).getExtensions().add(EXTENSION_NAME, extension);
    }
}
