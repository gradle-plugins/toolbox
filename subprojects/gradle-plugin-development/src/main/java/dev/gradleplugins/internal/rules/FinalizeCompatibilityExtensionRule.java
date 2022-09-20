package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.ProviderUtils.disallowChanges;

@RuleGroup(CompatibilityGroup.class)
// Finalizer rule because it fixes the value of extension properties
public final class FinalizeCompatibilityExtensionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        disallowChanges(compatibility(gradlePlugin(project)).getMinimumGradleVersion());
        disallowChanges(compatibility(gradlePlugin(project)).getGradleApiVersion());
    }
}
