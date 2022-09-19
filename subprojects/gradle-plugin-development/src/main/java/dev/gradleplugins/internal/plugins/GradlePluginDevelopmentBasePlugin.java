package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.rules.AddGradleApiDependencyToCompileOnlyApiConfigurationUsingCompatibilityInformationIfPresentRule;
import dev.gradleplugins.internal.rules.ConfigureGradleApiVersionConventionBasedOnMinimumGradleVersionRule;
import dev.gradleplugins.internal.rules.ConfigureMinimumGradleVersionConventionWithCurrentGradleVersionRule;
import dev.gradleplugins.internal.rules.FinalizeCompatibilityExtensionRule;
import dev.gradleplugins.internal.rules.RegisterCompatibilityExtensionGradlePluginDevelopmentExtensionRule;
import dev.gradleplugins.internal.rules.RemoveGradleApiSelfResolvingDependencyFromMainApiConfigurationRule;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

import static dev.gradleplugins.internal.util.ActionUtils.withoutParameter;

abstract /*final*/ class GradlePluginDevelopmentBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentBasePlugin() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentExtensionPlugin.class);
        project.getPluginManager().withPlugin("java-gradle-plugin", withoutParameter(() -> {
            new RegisterCompatibilityExtensionGradlePluginDevelopmentExtensionRule().execute(project);

            new RemoveGradleApiSelfResolvingDependencyFromMainApiConfigurationRule().execute(project);
            new ConfigureGradleApiVersionConventionBasedOnMinimumGradleVersionRule().execute(project);

            project.afterEvaluate(withoutParameter(() -> {
                new AddGradleApiDependencyToCompileOnlyApiConfigurationUsingCompatibilityInformationIfPresentRule().execute(project);
                new ConfigureMinimumGradleVersionConventionWithCurrentGradleVersionRule().execute(project);
                new FinalizeCompatibilityExtensionRule().execute(project);
            }));
        }));
        project.getPluginManager().withPlugin("java-gradle-plugin", new RemoveTestSourceSets(project));
    }
}
