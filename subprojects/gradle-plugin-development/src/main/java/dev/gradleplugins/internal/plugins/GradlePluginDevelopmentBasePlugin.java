package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.rules.AddGradleApiDependencyToCompileOnlyApiConfigurationUsingCompatibilityInformationIfPresentRule;
import dev.gradleplugins.internal.rules.ApiSourceSet_ClearPluginOutgoingApiElementsWhenApiSourceSetAvailableRule;
import dev.gradleplugins.internal.rules.VersionedSourceSet_AddGradleApiDependencyToCompileOnlyConfigurationOfEachVersionedSourceSetRule;
import dev.gradleplugins.internal.rules.VersionedSourceSet_AddVersionedComponentDependencyToPluginSourceSetAsImplementationDependencyRule;
import dev.gradleplugins.internal.rules.ConfigureGradleApiVersionConventionBasedOnMinimumGradleVersionRule;
import dev.gradleplugins.internal.rules.ConfigureMinimumGradleVersionConventionWithCurrentGradleVersionRule;
import dev.gradleplugins.internal.rules.ApiSourceSet_RegisterApiSourceSetAsJavaFeatureWhenAvailableRule;
import dev.gradleplugins.internal.rules.ApiSourceSet_LockApiSourceSetPropertyOnGradleExtensionPluginDevelopmentExtensionRule;
import dev.gradleplugins.internal.rules.FinalizeCompatibilityExtensionRule;
import dev.gradleplugins.internal.rules.ApiSourceSet_PluginDependsOnApiSourceSetIfAvailableRule;
import dev.gradleplugins.internal.rules.ApiSourceSet_RegisterApiSourceSetPropertyOnGradlePluginDevelopmentExtensionRule;
import dev.gradleplugins.internal.rules.RegisterCompatibilityExtensionGradlePluginDevelopmentExtensionRule;
import dev.gradleplugins.internal.rules.VersionedSourceSet_RegisterJvmFeatureForEachVersionedSourceSetRule;
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
            new VersionedSourceSet_RegisterJvmFeatureForEachVersionedSourceSetRule().execute(project);
            new ApiSourceSet_RegisterApiSourceSetPropertyOnGradlePluginDevelopmentExtensionRule().execute(project);

            new RemoveGradleApiSelfResolvingDependencyFromMainApiConfigurationRule().execute(project);
            new ConfigureGradleApiVersionConventionBasedOnMinimumGradleVersionRule().execute(project);
            new VersionedSourceSet_AddGradleApiDependencyToCompileOnlyConfigurationOfEachVersionedSourceSetRule().execute(project);

            project.afterEvaluate(withoutParameter(() -> {
                new ApiSourceSet_LockApiSourceSetPropertyOnGradleExtensionPluginDevelopmentExtensionRule().execute(project);
                new AddGradleApiDependencyToCompileOnlyApiConfigurationUsingCompatibilityInformationIfPresentRule().execute(project);
                new ApiSourceSet_PluginDependsOnApiSourceSetIfAvailableRule().execute(project);
                new ApiSourceSet_ClearPluginOutgoingApiElementsWhenApiSourceSetAvailableRule().execute(project);
                new ConfigureMinimumGradleVersionConventionWithCurrentGradleVersionRule().execute(project);
                new FinalizeCompatibilityExtensionRule().execute(project);
                new ApiSourceSet_RegisterApiSourceSetAsJavaFeatureWhenAvailableRule().execute(project);
                new VersionedSourceSet_AddVersionedComponentDependencyToPluginSourceSetAsImplementationDependencyRule().execute(project);
            }));
        }));
        project.getPluginManager().withPlugin("java-gradle-plugin", new RemoveTestSourceSets(project));
    }
}
