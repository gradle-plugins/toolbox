package dev.gradleplugins.internal.plugins;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

final class RegisterTestingExtensionOnGradleDevelExtensionRule implements Action<AppliedPlugin> {
    private final Project project;

    RegisterTestingExtensionOnGradleDevelExtensionRule(Project project) {
        this.project = project;
    }

    @Override
    public void execute(AppliedPlugin ignored) {
        val gradlePluginExtension = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        ((ExtensionAware) gradlePluginExtension).getExtensions().add("testing", project.getObjects().newInstance(DefaultGradlePluginDevelopmentTestingExtension.class));
    }
}
