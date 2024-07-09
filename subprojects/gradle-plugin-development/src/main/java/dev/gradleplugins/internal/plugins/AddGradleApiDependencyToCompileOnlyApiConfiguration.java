package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.DependencyBucketFactory;
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.util.LocalOrRemoteVersionTransformer;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.GradleVersion;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

public final class AddGradleApiDependencyToCompileOnlyApiConfiguration implements Action<Project> {
    @Override
    public void execute(Project project) {
        final DependencyFactory factory = new DependencyFactory(project.getDependencies());
        new DependencyBucketFactory(project, project.provider(() -> project.getExtensions().getByType(GradlePluginDevelopmentExtension.class).getPluginSourceSet())).create(compileOnlyApiBucketName()).add(project.provider(() -> compatibility(gradlePlugin(project))).flatMap(it -> it.getGradleApiVersion().orElse("local").map(localOrRemoteGradleApi(factory))));
    }

    private static String compileOnlyApiBucketName() {
        if (GradleVersion.current().compareTo(GradleVersion.version("6.7")) >= 0) {
            return "compileOnlyApi";
        }
        return "compileOnly";
    }

    private static Transformer<Dependency, String> localOrRemoteGradleApi(DependencyFactory factory) {
        return new LocalOrRemoteVersionTransformer<>(factory::localGradleApi, factory::gradleApi);
    }
}
