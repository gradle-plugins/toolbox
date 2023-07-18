package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import dev.gradleplugins.internal.AddDependency;
import dev.gradleplugins.internal.DependencyFactory;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.util.GradleVersion;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

public final class AddGradleApiDependencyToCompileOnlyApiConfiguration implements Action<AppliedPlugin> {
    private final Project project;

    public AddGradleApiDependencyToCompileOnlyApiConfiguration(Project project) {
        this.project = project;
    }

    @Override
    public void execute(AppliedPlugin ignored) {
        val factory = DependencyFactory.forProject(project);
        project.getConfigurations().named(getCompileOnlyApiConfigurationName(), new AddDependency(compatibility(gradlePlugin(project)).getGradleApiVersion().map(GradlePluginDevelopmentDependencyExtension.from(project.getDependencies())::gradleApi), factory));
    }

    private static String getCompileOnlyApiConfigurationName() {
        if (GradleVersion.current().compareTo(GradleVersion.version("6.7")) >= 0) {
            return "compileOnlyApi";
        }
        return "compileOnly";
    }
}
