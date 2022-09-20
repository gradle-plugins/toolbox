package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.util.GradleVersion;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.GradlePluginDevelopmentDependencyExtension.GRADLE_API_LOCAL_VERSION;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

@RuleGroup(CompatibilityGroup.class)
public final class ConfigureGradleApiVersionConventionBasedOnMinimumGradleVersionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        val extension = compatibility(gradlePlugin(project));
        extension.getGradleApiVersion().convention(extension.getMinimumGradleVersion().map(toLocalIfGradleSnapshotVersion())
                .orElse("local"));
    }

    private static Transformer<String, String> toLocalIfGradleSnapshotVersion() {
        return it -> {
            if (GradleVersion.version(it).isSnapshot()) {
                return GRADLE_API_LOCAL_VERSION;
            }
            return it;
        };
    }
}
