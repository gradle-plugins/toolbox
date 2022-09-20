package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

@RuleGroup(ApiSourceSetGroup.class)
// Finalizer rule because it needs plugin source set
public class ApiSourceSet_ClearPluginOutgoingApiElementsWhenApiSourceSetAvailableRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        project.getConfigurations().named(gradlePlugin(project).getPluginSourceSet().getApiElementsConfigurationName(), configuration -> {
            configuration.getOutgoing().getArtifacts().clear(); // clear base artifacts
            configuration.getOutgoing().getVariants().clear(); // clear classes artifacts
        });
    }
}
