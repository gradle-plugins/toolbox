package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

@RuleGroup(CompatibilityGroup.class)
public final class RegisterCompatibilityExtensionGradlePluginDevelopmentExtensionRule implements Action<Project> {
    private static final String EXTENSION_NAME = "compatibility";

    @Override
    public void execute(Project project) {
        ((ExtensionAware) gradlePlugin(project)).getExtensions()
                .create(EXTENSION_NAME, GradlePluginDevelopmentCompatibilityExtension.class);
    }
}
