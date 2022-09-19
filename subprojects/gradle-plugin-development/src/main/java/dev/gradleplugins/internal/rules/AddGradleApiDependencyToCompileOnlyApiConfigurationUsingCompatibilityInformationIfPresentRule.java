package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import dev.gradleplugins.internal.RuleGroup;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.GradleVersion;

import java.util.Optional;

import static dev.gradleplugins.internal.util.SourceSetUtils.compileOnlyApiConfigurationName;

@RuleGroup(ExternalGradleApiGroup.class)
// Finalizer rule because if rely on pluginSourceSet which may not be `main`
public final class AddGradleApiDependencyToCompileOnlyApiConfigurationUsingCompatibilityInformationIfPresentRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        final String configurationName = compileOnlyApiConfigurationName(gradlePlugin(project).getPluginSourceSet());
        val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.forProject(project);
        final Provider<Dependency> notation = project.provider(() -> compatibility(gradlePlugin(project)).orElse(null))
                .flatMap(GradlePluginDevelopmentCompatibilityExtension::getGradleApiVersion)
                .orElse(GradleVersion.current().getVersion())
                .map(dependencies::gradleApi);
        dependencies.add(configurationName, notation);
    }

    private static GradlePluginDevelopmentExtension gradlePlugin(Project project) {
        return (GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin");
    }

    private static Optional<GradlePluginDevelopmentCompatibilityExtension> compatibility(GradlePluginDevelopmentExtension extension) {
        return Optional.ofNullable((GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware) extension).getExtensions().findByName("compatibility"));
    }
}
