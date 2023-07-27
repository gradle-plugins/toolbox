package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket;
import dev.gradleplugins.GradlePluginDevelopmentDependencyModifiers;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteDependencies;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import dev.gradleplugins.GradleRuntimeCompatibility;
import dev.gradleplugins.TaskView;
import dev.gradleplugins.internal.AttachTestTasksToCheckTaskIfPresent;
import dev.gradleplugins.internal.ConfigurePluginUnderTestMetadataTask;
import dev.gradleplugins.internal.CreateTestTasksFromTestingStrategiesRule;
import dev.gradleplugins.internal.DefaultDependencyBucket;
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.EnforcedPlatformDependencyModifier;
import dev.gradleplugins.internal.FinalizableComponent;
import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.PlatformDependencyModifier;
import dev.gradleplugins.internal.PluginUnderTestMetadataConfigurationSupplier;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import dev.gradleplugins.internal.TestFixturesDependencyModifier;
import dev.gradleplugins.internal.TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule;
import dev.gradleplugins.internal.runtime.dsl.GroovyHelper;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.resources.TextResourceFactory;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;
import org.gradle.util.GUtil;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.DefaultDependencyVersions.SPOCK_FRAMEWORK_VERSION;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;
import static java.lang.String.format;

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

    public static final class DefaultGradlePluginDevelopmentTestSuiteFactory implements GradlePluginDevelopmentTestSuiteFactory {
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

        public abstract static class GradlePluginDevelopmentTestSuiteInternal implements GradlePluginDevelopmentTestSuite, SoftwareComponent, HasPublicType, FinalizableComponent {
            private static final String PLUGIN_UNDER_TEST_METADATA_TASK_NAME_PREFIX = "pluginUnderTestMetadata";
            private static final String PLUGIN_DEVELOPMENT_GROUP = "Plugin development";
            private static final String PLUGIN_UNDER_TEST_METADATA_TASK_DESCRIPTION_FORMAT = "Generates the metadata for plugin %s.";
            private final GradlePluginTestingStrategyFactory strategyFactory;
            private final Dependencies dependencies;
            private final String name;
            @Getter
            private final List<Action<? super Test>> testTaskActions = new ArrayList<>();
            private final List<Action<? super GradlePluginDevelopmentTestSuite>> finalizeActions = new ArrayList<>();
            private final TestTaskView testTasks;
            private final TaskProvider<PluginUnderTestMetadata> pluginUnderTestMetadataTask;
            private final String displayName;
            private boolean finalized = false;

            @Inject
            public GradlePluginDevelopmentTestSuiteInternal(String name, Project project, ProviderFactory providers, Provider<String> minimumGradleVersion, ReleasedVersionDistributions releasedVersions) {
                this.strategyFactory = new GradlePluginTestingStrategyFactoryInternal(minimumGradleVersion, releasedVersions);
                this.name = name;
                this.displayName = GUtil.toWords(name) + "s";
                this.dependencies = project.getObjects().newInstance(Dependencies.class, project, minimumGradleVersion.orElse(GradleVersion.current().getVersion()).map(GradleRuntimeCompatibility::groovyVersionOf), this);

                // adhoc decoration of the dependencies
                this.dependencies.forEach(dependencyBucket -> {
                    GroovyHelper.instance().addNewInstanceMethod(this.dependencies, dependencyBucket.getName(), new MethodClosure(dependencyBucket, "add"));
                });
                GroovyHelper.instance().addNewInstanceMethod(this.dependencies, "platform", new MethodClosure(this.dependencies.getPlatform(), "modify"));
                GroovyHelper.instance().addNewInstanceMethod(this.dependencies, "enforcedPlatform", new MethodClosure(this.dependencies.getEnforcedPlatform(), "modify"));
                GroovyHelper.instance().addNewInstanceMethod(this.dependencies, "testFixtures", new MethodClosure(this.dependencies.getTestFixtures(), "modify"));

                this.pluginUnderTestMetadataTask = registerPluginUnderTestMetadataTask(project.getTasks(), pluginUnderTestMetadataTaskName(name), displayName);
                this.testTasks = project.getObjects().newInstance(TestTaskView.class, testTaskActions, providers.provider(new FinalizeComponentCallable<>()).orElse(getTestTaskCollection()));
                this.finalizeActions.add(testSuite -> new PluginUnderTestMetadataConfigurationSupplier(project, testSuite).get());
                this.finalizeActions.add(new TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule());
                this.finalizeActions.add(new CreateTestTasksFromTestingStrategiesRule(project.getTasks(), project.getObjects(), getTestTaskCollection()));
                this.finalizeActions.add(new AttachTestTasksToCheckTaskIfPresent(project.getPluginManager(), project.getTasks()));
            }

            private static TaskProvider<PluginUnderTestMetadata> registerPluginUnderTestMetadataTask(TaskContainer tasks, String taskName, String displayName) {
                return tasks.register(taskName, PluginUnderTestMetadata.class, task -> {
                    task.setGroup(PLUGIN_DEVELOPMENT_GROUP);
                    task.setDescription(format(PLUGIN_UNDER_TEST_METADATA_TASK_DESCRIPTION_FORMAT, displayName));
                });
            }

            private static String pluginUnderTestMetadataTaskName(String testSuiteName) {
                return PLUGIN_UNDER_TEST_METADATA_TASK_NAME_PREFIX + StringUtils.capitalize(testSuiteName);
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public TypeOf<?> getPublicType() {
                return TypeOf.typeOf(GradlePluginDevelopmentTestSuite.class);
            }

            @Override
            public GradlePluginTestingStrategyFactory getStrategies() {
                return strategyFactory;
            }

            public abstract SetProperty<Test> getTestTaskCollection();

            @Override
            public String toString() {
                return "test suite '" + name + "'";
            }

            @Override
            public TaskView<Test> getTestTasks() {
                return testTasks;
            }

            @Override
            public TaskProvider<PluginUnderTestMetadata> getPluginUnderTestMetadataTask() {
                return pluginUnderTestMetadataTask;
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }

            protected static /*final*/ abstract class TestTaskView implements TaskView<Test> {
                private final List<Action<? super Test>> testTaskActions;
                private final Provider<Set<Test>> elementsProvider;

                @Inject
                public TestTaskView(List<Action<? super Test>> testTaskActions, Provider<Set<Test>> elementsProvider) {
                    this.testTaskActions = testTaskActions;
                    this.elementsProvider = elementsProvider;
                }

                @Override
                public void configureEach(Action<? super Test> action) {
                    testTaskActions.add(action);
                }

                @Override
                public Provider<Set<Test>> getElements() {
                    return elementsProvider;
                }
            }

            @Override
            public void finalizeComponent() {
                if (!finalized) {
                    finalized = true;
                    finalizeActions.forEach(it -> it.execute(this));
                    getSourceSet().finalizeValue();
                }
            }

            @Override
            public boolean isFinalized() {
                return finalized;
            }

            public void whenFinalized(Action<? super GradlePluginDevelopmentTestSuite> action) {
                finalizeActions.add(action);
            }

            @Override
            public Dependencies getDependencies() {
                return dependencies;
            }

            @Override
            public void dependencies(Action<? super GradlePluginDevelopmentTestSuiteDependencies> action) {
                action.execute(dependencies);
            }

            protected static abstract /*final*/ class Dependencies implements GradlePluginDevelopmentTestSuiteDependencies, Iterable<GradlePluginDevelopmentDependencyBucket> {
                private final Map<String, GradlePluginDevelopmentDependencyBucket> dependencyBuckets = new LinkedHashMap<>();
                private final PluginManager pluginManager;
                private final Provider<String> defaultGroovyVersion;
                private final DependencyFactory dependencyFactory;
                private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier platformDependencyModifier;
                private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier enforcedPlatformDependencyModifier;
                private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier testFixturesDependencyModifier;
                private final Project project;

                @Inject
                protected abstract DependencyHandler getDependencies();

                @Inject
                public Dependencies(Project project, Provider<String> defaultGroovyVersion, GradlePluginDevelopmentTestSuite testSuite) {
                    this.project = project;
                    add(new DefaultDependencyBucket(project, testSuite.getSourceSet(), "implementation"));
                    add(new DefaultDependencyBucket(project, testSuite.getSourceSet(), "compileOnly"));
                    add(new DefaultDependencyBucket(project, testSuite.getSourceSet(), "runtimeOnly"));
                    add(new DefaultDependencyBucket(project, testSuite.getSourceSet(), "annotationProcessor"));
                    add(new DefaultDependencyBucket(project, testSuite.getSourceSet(), "pluginUnderTestMetadata"));
                    this.platformDependencyModifier = new PlatformDependencyModifier(project);
                    this.enforcedPlatformDependencyModifier = new EnforcedPlatformDependencyModifier(project);
                    this.testFixturesDependencyModifier = new TestFixturesDependencyModifier(project);
                    this.pluginManager = project.getPluginManager();
                    this.defaultGroovyVersion = defaultGroovyVersion;
                    this.dependencyFactory = DependencyFactory.forProject(project);
                }

                private void add(GradlePluginDevelopmentDependencyBucket dependencyBucket) {
                    dependencyBuckets.put(dependencyBucket.getName(), dependencyBucket);
                }

                @Override
                public GradlePluginDevelopmentDependencyBucket getImplementation() {
                    return dependencyBuckets.get("implementation");
                }

                @Override
                public GradlePluginDevelopmentDependencyBucket getCompileOnly() {
                    return dependencyBuckets.get("compileOnly");
                }

                @Override
                public GradlePluginDevelopmentDependencyBucket getRuntimeOnly() {
                    return dependencyBuckets.get("runtimeOnly");
                }

                @Override
                public GradlePluginDevelopmentDependencyBucket getAnnotationProcessor() {
                    return dependencyBuckets.get("annotationProcessor");
                }

                @Override
                public GradlePluginDevelopmentDependencyBucket getPluginUnderTestMetadata() {
                    return dependencyBuckets.get("pluginUnderTestMetadata");
                }

                @Override
                public GradlePluginDevelopmentDependencyModifiers.DependencyModifier getPlatform() {
                    return platformDependencyModifier;
                }

                @Override
                public GradlePluginDevelopmentDependencyModifiers.DependencyModifier getEnforcedPlatform() {
                    return enforcedPlatformDependencyModifier;
                }

                @Override
                public GradlePluginDevelopmentDependencyModifiers.DependencyModifier getTestFixtures() {
                    return testFixturesDependencyModifier;
                }

                @Override
                public Iterator<GradlePluginDevelopmentDependencyBucket> iterator() {
                    return dependencyBuckets.values().iterator();
                }

                @Override
                public Dependency spockFramework() {
                    return spockFramework(SPOCK_FRAMEWORK_VERSION);
                }

                @Override
                public Dependency spockFramework(String version) {
                    pluginManager.apply("groovy-base"); // Spock framework imply Groovy implementation language
                    return dependencyFactory.create("org.spockframework:spock-core:" + version);
                }

                @Override
                @SuppressWarnings("deprecation")
                public Dependency gradleFixtures() {
                    return dependencyFactory.gradleFixtures();
                }

                @Override
                public Dependency gradleTestKit() {
                    return dependencyFactory.localGradleTestKit();
                }

                @Override
                public Dependency gradleTestKit(String version) {
                    if ("local".equals(version)) {
                        return dependencyFactory.localGradleTestKit();
                    }
                    return dependencyFactory.gradleTestKit(version);
                }

                @Override
                public Provider<Dependency> groovy() {
                    return defaultGroovyVersion.map(this::groovy);
                }

                @Override
                public Dependency groovy(String version) {
                    return dependencyFactory.create("org.codehaus.groovy:groovy-all:" + version);
                }

                @Override
                public Dependency gradleApi(String version) {
                    if ("local".equals(version)) {
                        return dependencyFactory.localGradleApi();
                    }
                    return dependencyFactory.gradleApi(version);
                }

                @Override
                public ProjectDependency project(String projectPath) {
                    return dependencyFactory.create(project.project(projectPath));
                }

                @Override
                public ProjectDependency project() {
                    return dependencyFactory.create(project);
                }
            }

            private final class FinalizeComponentCallable<T> implements Callable<T> {
                @Override
                public T call() throws Exception {
                    finalizeComponent();
                    return null;
                }
            }
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
