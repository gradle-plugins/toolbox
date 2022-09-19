package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import dev.gradleplugins.internal.util.GradleUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.util.GradleVersion;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradleUtils.currentGradleVersion;

@RuleGroup(CompatibilityGroup.class)
// Finalizer rule because it sets a value to the minimum Gradle version
//   must run before FinalizeCompatibilityExtensionRule
//   must run after FinalizeJvmCompatibilityExtensionRule
public final class ConfigureMinimumGradleVersionConventionWithCurrentGradleVersionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        val extension = compatibility(gradlePlugin(project));
        extension.getMinimumGradleVersion().convention(currentGradleVersion().getVersion());
    }
}
