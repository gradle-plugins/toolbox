package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.GradleVersion;

import static dev.gradleplugins.GradlePluginDevelopmentDependencyExtension.GRADLE_API_LOCAL_VERSION;

public final class RegisterGradlePluginDevelopmentCompatibilityExtensionRule implements Action<Project> {
    private static final String EXTENSION_NAME = "compatibility";

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void execute(Project project) {
        final GradlePluginDevelopmentCompatibilityExtension compatibilityExtension = project.getObjects().newInstance(GradlePluginDevelopmentCompatibilityExtension.class);

        compatibilityExtension.getMinimumGradleVersion().finalizeValueOnRead();
        compatibilityExtension.getGradleApiVersion().finalizeValueOnRead();
        compatibilityExtension.getGradleApiVersion().convention(compatibilityExtension.getMinimumGradleVersion().map(toLocalIfGradleSnapshotVersion()).orElse("local"));

        gradlePlugin(project, developmentExtension -> {
            ((ExtensionAware) developmentExtension).getExtensions().add(EXTENSION_NAME, compatibilityExtension);
        });

        project.afterEvaluate(__ -> {
            compatibilityExtension.getGradleApiVersion().disallowChanges();
            compatibilityExtension.getMinimumGradleVersion().disallowChanges();
        });
    }

    private static void gradlePlugin(Project project, Action<? super GradlePluginDevelopmentExtension> action) {
        action.execute((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin"));
    }

    private static Transformer<String, String> toLocalIfGradleSnapshotVersion() {
        return it -> {
            if (GradleVersion.version(it).isSnapshot()) {
                return GRADLE_API_LOCAL_VERSION;
            } else if (it.contains("-rc-")) {
                return GRADLE_API_LOCAL_VERSION;
            } else if (it.contains("-milestone-")) {
                return GRADLE_API_LOCAL_VERSION;
            }
            return it;
        };
    }
}
