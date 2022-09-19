package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;

import static dev.gradleplugins.internal.rules.JvmCompatibilityExtension.jvm;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static dev.gradleplugins.internal.util.ProviderUtils.finalizeValue;

@RuleGroup(CompatibilityGroup.class)
// Finalizer rule because it sync the values for source/target compatibility on extension
public final class FinalizeJvmCompatibilityExtensionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        java(project).setSourceCompatibility(finalizeValue(jvm(gradlePlugin(project)).getSourceCompatibility()).get());
        java(project).setTargetCompatibility(finalizeValue(jvm(gradlePlugin(project)).getTargetCompatibility()).get());
    }
}
