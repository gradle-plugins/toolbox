package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.apiSourceSet;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.ProviderUtils.disallowChanges;

@RuleGroup(ApiSourceSetGroup.class)
// Finalizer rule because it locks api source set
public final class ApiSourceSet_LockApiSourceSetPropertyOnGradleExtensionPluginDevelopmentExtensionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        disallowChanges(apiSourceSet(gradlePlugin(project)));
    }
}
