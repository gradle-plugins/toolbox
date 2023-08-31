package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.internal.DefaultGradlePluginDevelopmentTestSuiteFactory;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.ClasspathNormalizer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.component.local.model.OpaqueComponentIdentifier;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

public final class RegisterTestSuiteFactoryServiceRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        final DomainObjectSet<GradlePluginDevelopmentTestSuite> testSuites = project.getObjects().domainObjectSet(GradlePluginDevelopmentTestSuite.class);
        project.getExtensions().add(GradlePluginDevelopmentTestSuiteFactory.class, "testSuiteFactory", new CapturingGradlePluginDevelopmentTestSuiteFactory(testSuites, new DefaultGradlePluginDevelopmentTestSuiteFactory(project)));

        project.afterEvaluate(__ -> {
            testSuites.configureEach(new FinalizeTestSuiteProperties());
            testSuites.configureEach(testSuite -> new PluginUnderTestMetadataConfigurationSupplier(project, testSuite).get());
            testSuites.configureEach(new TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule());
            testSuites.configureEach(new AttachTestTasksToCheckTaskIfPresent(project.getPluginManager(), project.getTasks()));

            // Register as finalized action because it adds configuration which early finalize source set property
            testSuites.configureEach(new ConfigurePluginUnderTestMetadataTask(project));
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

    @SuppressWarnings("UnstableApiUsage")
    public static final class FinalizeTestSuiteProperties implements Action<GradlePluginDevelopmentTestSuite> {
        @Override
        public void execute(GradlePluginDevelopmentTestSuite testSuite) {
            testSuite.getTestedSourceSet().disallowChanges();
            testSuite.getSourceSet().disallowChanges();
            testSuite.getTestingStrategies().disallowChanges();
        }
    }

    public static final class AttachTestTasksToCheckTaskIfPresent implements Action<GradlePluginDevelopmentTestSuite> {
        private final PluginManager pluginManager;
        private final TaskContainer tasks;

        public AttachTestTasksToCheckTaskIfPresent(PluginManager pluginManager, TaskContainer tasks) {
            this.pluginManager = pluginManager;
            this.tasks = tasks;
        }

        @Override
        public void execute(GradlePluginDevelopmentTestSuite testSuite) {
            if (pluginManager.hasPlugin("java-base")) {
                tasks.named("check", task -> task.dependsOn(testSuite.getTestTasks().getElements()));
            }
        }
    }

    public static final class TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule implements Action<GradlePluginDevelopmentTestSuite> {
        @Override
        public void execute(GradlePluginDevelopmentTestSuite testSuite) {
            testSuite.getTestedSourceSet().disallowChanges();
            if (testSuite.getTestedSourceSet().isPresent()) {
                SourceSet sourceSet = testSuite.getSourceSet().get();
                SourceSet testedSourceSet = testSuite.getTestedSourceSet().get();
                sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(testedSourceSet.getOutput()));
                sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(sourceSet.getOutput()).plus(sourceSet.getCompileClasspath()));
            }
        }
    }

    public static final class ConfigurePluginUnderTestMetadataTask implements Action<GradlePluginDevelopmentTestSuite> {
        private final Project project;

        public ConfigurePluginUnderTestMetadataTask(Project project) {
            this.project = project;
        }

        @Override
        public void execute(GradlePluginDevelopmentTestSuite testSuite) {
            final Provider<Configuration> pluginUnderTestMetadata = testSuite.getDependencies().getPluginUnderTestMetadata().getAsConfiguration();
            testSuite.getPluginUnderTestMetadataTask().configure(task -> {
                task.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir(task.getName()));
                task.getPluginClasspath().from(testSuite.getTestedSourceSet().map(asPluginClasspath(project)).orElse(Collections.emptyList()));
                task.getPluginClasspath().from(pluginUnderTestMetadata);
            });

            ignorePluginUnderTestMetadataFile(project);
            configurePluginUnderTestMetadataAsTestInputs(testSuite);
            configurePluginUnderTestMetadataAsRuntimeOnlyDependencies(project, testSuite);
        }

        private static void configurePluginUnderTestMetadataAsRuntimeOnlyDependencies(Project project, GradlePluginDevelopmentTestSuite testSuite) {
            // Consider adding gradleTestKit to implementation as per java-gradle-plugin
            testSuite.getDependencies().getRuntimeOnly().add(project.getLayout().files(testSuite.getPluginUnderTestMetadataTask()));
        }

        private static void configurePluginUnderTestMetadataAsTestInputs(GradlePluginDevelopmentTestSuite testSuite) {
            testSuite.getTestTasks().configureEach(task -> task.getInputs()
                    .files(testSuite.getPluginUnderTestMetadataTask().map(PluginUnderTestMetadata::getPluginClasspath))
                    .withPropertyName("pluginUnderTestClasspath") // different from JavaGradlePluginPlugin, e.g. pluginClasspath, to avoid conflict
                    .withNormalizer(ClasspathNormalizer.class));
        }

        private static void ignorePluginUnderTestMetadataFile(Project project) {
            project.getNormalization().getRuntimeClasspath().ignore(PluginUnderTestMetadata.METADATA_FILE_NAME);
        }

        private static Transformer<Object, SourceSet> asPluginClasspath(Project project) {
            return sourceSet -> {
                val runtimeClasspath = project.getConfigurations().getByName(sourceSet.getRuntimeClasspathConfigurationName());
                val view = runtimeClasspath.getIncoming().artifactView(config -> {
                    config.componentFilter(componentId -> {
                        if (componentId instanceof OpaqueComponentIdentifier) {
                            return !componentId.getDisplayName().equals("Gradle API")
                                    && !componentId.getDisplayName().equals("Local Groovy");
                        }
                        return true;
                    });
                });
                return sourceSet.getOutput().plus(view.getFiles());
            };
        }
    }

    public static final class PluginUnderTestMetadataConfigurationSupplier implements Supplier<NamedDomainObjectProvider<Configuration>> {
        private final Project project;
        private final GradlePluginDevelopmentTestSuite testSuite;
        private NamedDomainObjectProvider<Configuration> pluginUnderTestMetadata;

        public PluginUnderTestMetadataConfigurationSupplier(Project project, GradlePluginDevelopmentTestSuite testSuite) {
            this.project = project;
            this.testSuite = testSuite;
        }

        private SourceSet sourceSet() {
            return testSuite.getSourceSet().get();
        }

        @Override
        public NamedDomainObjectProvider<Configuration> get() {
            if (pluginUnderTestMetadata == null) {
                final Configuration configuration = project.getConfigurations().maybeCreate(sourceSet().getName() + "PluginUnderTestMetadata");
                configuration.setCanBeResolved(true);
                configuration.setCanBeConsumed(false);
                configuration.attributes(attributes -> attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME)));
                configuration.setDescription("Plugin under test metadata for source set '" + sourceSet().getName() + "'.");
                pluginUnderTestMetadata = project.getConfigurations().named(configuration.getName());
            }
            return pluginUnderTestMetadata;
        }
    }
}
