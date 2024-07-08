package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.util.GradleVersion;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

public final class AddGradleApiDependencyToCompileOnlyApiConfiguration implements Action<Project> {
    @Override
    public void execute(Project project) {
        final DependencyFactory factory = new DependencyFactory(project.getDependencies());
        val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.of(project.getDependencies());
        dependencies.add(getCompileOnlyApiConfigurationName(), project.provider(() -> compatibility(gradlePlugin(project))).flatMap(it -> it.getGradleApiVersion().orElse("local").map(version -> {
            return version.equals("local") ? factory.localGradleApi() : factory.gradleApi(version);
        })));
    }

    private static String getCompileOnlyApiConfigurationName() {
        if (GradleVersion.current().compareTo(GradleVersion.version("6.7")) >= 0) {
            return "compileOnlyApi";
        }
        return "compileOnly";
    }
}
