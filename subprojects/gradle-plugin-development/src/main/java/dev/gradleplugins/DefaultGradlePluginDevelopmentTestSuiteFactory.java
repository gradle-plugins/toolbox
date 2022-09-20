package dev.gradleplugins;

import dev.gradleplugins.internal.ConfigurePluginUnderTestMetadataTask;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.resources.TextResourceFactory;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import javax.inject.Inject;
import java.util.Objects;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.ProviderUtils.finalizeValueOnRead;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;

final class DefaultGradlePluginDevelopmentTestSuiteFactory implements GradlePluginDevelopmentTestSuiteFactory {
    private final Project project;

    DefaultGradlePluginDevelopmentTestSuiteFactory(Project project) {
        this.project = project;
    }

    @Override
    public GradlePluginDevelopmentTestSuite create(String name) {
        val result = project.getObjects().newInstance(GradlePluginDevelopmentTestSuiteInternal.class, name, project, minimumGradleVersion(project), gradleDistributions());
        // Register as finalized action because it adds configuration which early finalize source set property
        result.whenFinalized(new ConfigurePluginUnderTestMetadataTask(project));
        result.getSourceSet().convention(sourceSets(project).map(it -> it.maybeCreate(name)).orElse(project.provider(() -> {
            throw new RuntimeException("Please apply 'java-base' plugin.");
        })));
        result.getTestedSourceSet().convention(project.provider(() -> {
            if (project.getPluginManager().hasPlugin("java-gradle-plugin")) {
                return gradlePlugin(project).getPluginSourceSet();
            }
            return null;
        }));
        return result;
    }

    private ReleasedVersionDistributions gradleDistributions() {
        if (System.getProperties().containsKey("dev.gradleplugins.internal.use-text-resource")) {
            return new ReleasedVersionDistributions(project.getResources().getText());
        } else if (System.getProperties().containsKey("dev.gradleplugins.internal.use-build-service")) {
            return project.getGradle().getSharedServices().registerIfAbsent("gradleDistributions", Service.class, it -> {}).get().initialized(project.getResources().getText()).get();
        } else {
            return ReleasedVersionDistributions.GRADLE_DISTRIBUTIONS;
        }
    }

    static abstract class Service implements BuildService<BuildServiceParameters.None> {
        private volatile ReleasedVersionDistributions gradleDistributions = null;

        @Inject
        public Service() {}

        public Service initialized(TextResourceFactory textResourceFactory) {
            if (gradleDistributions == null) {
                synchronized (this) {
                    if (gradleDistributions == null) {
                        gradleDistributions = new ReleasedVersionDistributions(textResourceFactory);
                    }
                }
            }
            return this;
        }

        public ReleasedVersionDistributions get() {
            return Objects.requireNonNull(gradleDistributions);
        }
    }

    private static Provider<String> minimumGradleVersion(Project project) {
        return project.getObjects().newInstance(MinimumGradleVersionProvider.class)
                .getMinimumGradleVersion()
                .value(ofDevelMinimumGradleVersionIfAvailable(project));
    }

    private static Provider<String> ofDevelMinimumGradleVersionIfAvailable(Project project) {
        return project.provider(() -> {
            if (project.getPluginManager().hasPlugin("java-gradle-plugin") && project.getPluginManager().hasPlugin("dev.gradleplugins.gradle-plugin-base")) {
                return finalizeValueOnRead(compatibility(gradlePlugin(project)).getMinimumGradleVersion());
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
