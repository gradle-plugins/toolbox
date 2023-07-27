package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.internal.ConfigurePluginUnderTestMetadataTask;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.resources.TextResourceFactory;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Set;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;

public final class RegisterTestSuiteFactoryServiceRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        final DomainObjectSet<GradlePluginDevelopmentTestSuite> testSuites = project.getObjects().domainObjectSet(GradlePluginDevelopmentTestSuite.class);
        project.getExtensions().add(GradlePluginDevelopmentTestSuiteFactory.class, "testSuiteFactory", new CapturingGradlePluginDevelopmentTestSuiteFactory(testSuites, new DefaultGradlePluginDevelopmentTestSuiteFactory(project)));

        project.afterEvaluate(__ -> {
            testSuites.configureEach(new FinalizeTestSuiteProperties());
        });
    }

    private static final class CapturingGradlePluginDevelopmentTestSuiteFactory implements GradlePluginDevelopmentTestSuiteFactory {
        private final Set<GradlePluginDevelopmentTestSuite> testSuites;
        private final GradlePluginDevelopmentTestSuiteFactory delegate;

        CapturingGradlePluginDevelopmentTestSuiteFactory(Set<GradlePluginDevelopmentTestSuite> testSuites, GradlePluginDevelopmentTestSuiteFactory delegate) {
            this.testSuites = testSuites;
            this.delegate = delegate;
        }

        @Override
        public GradlePluginDevelopmentTestSuite create(String name) {
            final GradlePluginDevelopmentTestSuite result = delegate.create(name);
            testSuites.add(result);
            return result;
        }
    }

    private static final class DefaultGradlePluginDevelopmentTestSuiteFactory implements GradlePluginDevelopmentTestSuiteFactory {
        private final Project project;

        DefaultGradlePluginDevelopmentTestSuiteFactory(Project project) {
            this.project = project;
        }

        @Override
        public GradlePluginDevelopmentTestSuite create(String name) {
            val result = project.getObjects().newInstance(GradlePluginDevelopmentTestSuiteInternal.class, name, project, minimumGradleVersion(project), gradleDistributions());
            // Register as finalized action because it adds configuration which early finalize source set property
            result.whenFinalized(new ConfigurePluginUnderTestMetadataTask(project));
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

            result.getSourceSet().finalizeValueOnRead();
            result.getTestedSourceSet().finalizeValueOnRead();

            project.afterEvaluate(__ -> result.finalizeComponent());
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
                    return compatibility(gradlePlugin(project)).getMinimumGradleVersion().orElse(GradleVersion.current().getVersion());
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

    @SuppressWarnings("UnstableApiUsage")
    public static final class FinalizeTestSuiteProperties implements Action<GradlePluginDevelopmentTestSuite> {
        @Override
        public void execute(GradlePluginDevelopmentTestSuite testSuite) {
            testSuite.getTestedSourceSet().disallowChanges();
            testSuite.getSourceSet().disallowChanges();
            testSuite.getTestingStrategies().disallowChanges();
        }
    }
}
