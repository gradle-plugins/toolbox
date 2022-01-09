package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;

final class DefaultGradlePluginDevelopmentTestSuiteFactory implements GradlePluginDevelopmentTestSuiteFactory {
    private final Project project;

    DefaultGradlePluginDevelopmentTestSuiteFactory(Project project) {
        this.project = project;
    }

    @Override
    public GradlePluginDevelopmentTestSuite create(String name) {
        val result = project.getObjects().newInstance(GradlePluginDevelopmentTestSuiteInternal.class, name, minimumGradleVersion(project));
        result.getSourceSet().convention(project.provider(() -> {
            if (project.getPluginManager().hasPlugin("java-base")) {
                return sourceSets(project).maybeCreate(name);
            } else {
                throw new RuntimeException("Please apply 'java-base' plugin.");
            }
        }));
        result.getTestedSourceSet().convention(project.provider(() -> {
            if (project.getPluginManager().hasPlugin("java-gradle-plugin")) {
                return gradlePlugin(project).getPluginSourceSet();
            }
            return null;
        }));
        return result;
    }

    private static Provider<String> minimumGradleVersion(Project project) {
        return project.getObjects().newInstance(MinimumGradleVersionProvider.class)
                .getMinimumGradleVersion()
                .value(ofDevelMinimumGradleVersionIfAvailable(project));
    }

    private static Provider<String> ofDevelMinimumGradleVersionIfAvailable(Project project) {
        return project.provider(() -> {
            if (project.getPluginManager().hasPlugin("java-gradle-plugin") && project.getPluginManager().hasPlugin("dev.gradleplugins.gradle-plugin-base")) {
                return compatibility(gradlePlugin(project)).getMinimumGradleVersion();
            } else {
                return Providers.<String>notDefined(); // no minimum Gradle version...
            }
        }).flatMap(noOpTransformer());
    }

    private static <T> Transformer<T, T> noOpTransformer() {
        return it -> it;
    }

    protected interface MinimumGradleVersionProvider {
        Property<String> getMinimumGradleVersion();
    }
}
