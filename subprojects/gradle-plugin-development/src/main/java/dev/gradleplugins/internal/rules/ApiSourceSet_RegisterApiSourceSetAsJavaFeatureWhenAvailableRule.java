package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.apiSourceSet;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static dev.gradleplugins.internal.util.ProviderUtils.ifPresent;

@RuleGroup(ApiSourceSetGroup.class)
// Finalizer rule because the final value of apiSourceSet and pluginSourceSet is required
public final class ApiSourceSet_RegisterApiSourceSetAsJavaFeatureWhenAvailableRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        ifPresent(apiSourceSet(gradlePlugin(project)), sourceSet -> {
            // Maybe be another rule???
            java(project).registerFeature("api", spec -> {
                spec.usingSourceSet(sourceSet);
            });
        });
    }
}
