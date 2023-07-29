package dev.gradleplugins.internal;

import dev.gradleplugins.CompositeGradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket;
import dev.gradleplugins.GradlePluginDevelopmentDependencyModifiers;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteDependencies;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import dev.gradleplugins.GradleRuntimeCompatibility;
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import dev.gradleplugins.TaskView;
import dev.gradleplugins.internal.runtime.dsl.GroovyHelper;
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
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.resources.TextResourceFactory;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;
import org.gradle.util.GUtil;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.DefaultDependencyVersions.SPOCK_FRAMEWORK_VERSION;
import static dev.gradleplugins.internal.RegisterTestingStrategyPropertyExtensionRule.testingStrategyProperty;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

public final class DefaultGradlePluginDevelopmentTestSuiteFactory implements GradlePluginDevelopmentTestSuiteFactory {
    private final Project project;

    public DefaultGradlePluginDevelopmentTestSuiteFactory(Project project) {
        this.project = project;
    }

    @Override
    public GradlePluginDevelopmentTestSuite create(String name) {
        val result = project.getObjects().newInstance(GradlePluginDevelopmentTestSuiteInternal.class, name, project, minimumGradleVersion(project), gradleDistributions(), new DecoratingGradlePluginDevelopmentTestSuiteDependenciesFactory<>(new DefaultGradlePluginDevelopmentTestSuiteDependenciesFactory(project)));
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

        result.getTestingStrategies().finalizeValueOnRead();
        result.getSourceSet().finalizeValueOnRead();
        result.getTestedSourceSet().finalizeValueOnRead();

        project.afterEvaluate(__ -> {
            result.getTestTasks().realize();
            result.getSourceSet().getOrNull(); // force realized
        });
        return result;
    }

    private ReleasedVersionDistributions gradleDistributions() {
        if (System.getProperties().containsKey("dev.gradleplugins.internal.use-text-resource")) {
            return new ReleasedVersionDistributions(project.getResources().getText());
        } else if (System.getProperties().containsKey("dev.gradleplugins.internal.use-build-service")) {
            return project.getGradle().getSharedServices().registerIfAbsent("gradleDistributions", Service.class, it -> {
            }).get().initialized(project.getResources().getText()).get();
        } else {
            return ReleasedVersionDistributions.GRADLE_DISTRIBUTIONS;
        }
    }

    static abstract class Service implements BuildService<BuildServiceParameters.None> {
        private volatile ReleasedVersionDistributions gradleDistributions = null;

        @Inject
        public Service() {
        }

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

    interface GradlePluginDevelopmentTestSuiteDependenciesFactory<DependenciesType extends GradlePluginDevelopmentTestSuiteDependencies> {
        DependenciesType create(GradlePluginDevelopmentTestSuite testSuite);
    }

    private static final class DefaultGradlePluginDevelopmentTestSuiteDependenciesFactory implements GradlePluginDevelopmentTestSuiteDependenciesFactory<DefaultGradlePluginDevelopmentTestSuiteDependenciesFactory.Dependencies> {
        private final Project project;

        private DefaultGradlePluginDevelopmentTestSuiteDependenciesFactory(Project project) {
            this.project = project;
        }

        @Override
        public DefaultGradlePluginDevelopmentTestSuiteDependenciesFactory.Dependencies create(GradlePluginDevelopmentTestSuite testSuite) {
            return project.getObjects().newInstance(DefaultGradlePluginDevelopmentTestSuiteDependenciesFactory.Dependencies.class, project, minimumGradleVersion(project).orElse(GradleVersion.current().getVersion()).map(GradleRuntimeCompatibility::groovyVersionOf), new DefaultDependencyBucketFactory(project, testSuite.getSourceSet()));
        }

        protected static abstract /*final*/ class Dependencies implements GradlePluginDevelopmentTestSuiteDependencies, Iterable<GradlePluginDevelopmentDependencyBucket> {
            private final Map<String, GradlePluginDevelopmentDependencyBucket> dependencyBuckets = new LinkedHashMap<>();
            private final Provider<String> defaultGroovyVersion;
            private final DependencyFactory dependencyFactory;
            private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier platformDependencyModifier;
            private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier enforcedPlatformDependencyModifier;
            private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier testFixturesDependencyModifier;
            private final Project project;

            @Inject
            protected abstract DependencyHandler getDependencies();

            @Inject
            public Dependencies(Project project, Provider<String> defaultGroovyVersion, DependencyBucketFactory dependencyBucketFactory) {
                this.project = project;
                add(dependencyBucketFactory.create("implementation"));
                add(dependencyBucketFactory.create("compileOnly"));
                add(dependencyBucketFactory.create("runtimeOnly"));
                add(dependencyBucketFactory.create("annotationProcessor"));
                add(dependencyBucketFactory.create("pluginUnderTestMetadata"));
                this.platformDependencyModifier = new PlatformDependencyModifier(project);
                this.enforcedPlatformDependencyModifier = new EnforcedPlatformDependencyModifier(project);
                this.testFixturesDependencyModifier = new TestFixturesDependencyModifier(project);
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
                project.getPluginManager().apply("groovy-base"); // Spock framework imply Groovy implementation language
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
    }

    private static final class DecoratingGradlePluginDevelopmentTestSuiteDependenciesFactory<DependenciesType extends GradlePluginDevelopmentTestSuiteDependencies & Iterable<GradlePluginDevelopmentDependencyBucket>> implements GradlePluginDevelopmentTestSuiteDependenciesFactory<DependenciesType> {
        private final GradlePluginDevelopmentTestSuiteDependenciesFactory<DependenciesType> delegate;

        private DecoratingGradlePluginDevelopmentTestSuiteDependenciesFactory(GradlePluginDevelopmentTestSuiteDependenciesFactory<DependenciesType> delegate) {
            this.delegate = delegate;
        }

        @Override
        public DependenciesType create(GradlePluginDevelopmentTestSuite testSuite) {
            final DependenciesType result = delegate.create(testSuite);

            // adhoc decoration of the dependencies
            result.forEach(dependencyBucket -> {
                GroovyHelper.instance().addNewInstanceMethod(result, dependencyBucket.getName(), new MethodClosure(dependencyBucket, "add"));
            });
            GroovyHelper.instance().addNewInstanceMethod(result, "platform", new MethodClosure(result.getPlatform(), "modify"));
            GroovyHelper.instance().addNewInstanceMethod(result, "enforcedPlatform", new MethodClosure(result.getEnforcedPlatform(), "modify"));
            GroovyHelper.instance().addNewInstanceMethod(result, "testFixtures", new MethodClosure(result.getTestFixtures(), "modify"));

            return result;
        }
    }

    public abstract static class GradlePluginDevelopmentTestSuiteInternal implements GradlePluginDevelopmentTestSuite, SoftwareComponent, HasPublicType {
        private static final String PLUGIN_UNDER_TEST_METADATA_TASK_NAME_PREFIX = "pluginUnderTestMetadata";
        private static final String PLUGIN_DEVELOPMENT_GROUP = "Plugin development";
        private static final String PLUGIN_UNDER_TEST_METADATA_TASK_DESCRIPTION_FORMAT = "Generates the metadata for plugin %s.";
        private final GradlePluginTestingStrategyFactory strategyFactory;
        private final GradlePluginDevelopmentTestSuiteDependencies dependencies;
        private final String name;
        private final GradlePluginDevelopmentTestSuiteInternal.TestTaskView testTasks;
        private final TaskProvider<PluginUnderTestMetadata> pluginUnderTestMetadataTask;
        private final String displayName;

        @Inject
        public GradlePluginDevelopmentTestSuiteInternal(String name, Project project, Provider<String> minimumGradleVersion, ReleasedVersionDistributions releasedVersions, GradlePluginDevelopmentTestSuiteDependenciesFactory<? extends GradlePluginDevelopmentTestSuiteDependencies> dependenciesFactory) {
            this.strategyFactory = new GradlePluginTestingStrategyFactoryInternal(minimumGradleVersion, releasedVersions);
            this.name = name;
            this.displayName = GUtil.toWords(name) + "s";
            this.dependencies = dependenciesFactory.create(this);
            this.pluginUnderTestMetadataTask = registerPluginUnderTestMetadataTask(project.getTasks(), pluginUnderTestMetadataTaskName(name), displayName);
            this.testTasks = project.getObjects().newInstance(GradlePluginDevelopmentTestSuiteInternal.TestTaskView.class, project, getTestSpecs());

            getTestSpecs().addAll(getTestingStrategies().map(new CreateTestTasksFromTestingStrategiesTransformer(project)));
            getTestSpecs().disallowChanges(); // do not allow rewire
            getTestSpecs().finalizeValueOnRead();
        }

        private final class CreateTestTasksFromTestingStrategiesTransformer implements Transformer<Iterable<GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec>, Set<GradlePluginTestingStrategy>> {
            private final GradlePluginDevelopmentTestSuite testSuite = GradlePluginDevelopmentTestSuiteInternal.this;
            private final Project project;

            private CreateTestTasksFromTestingStrategiesTransformer(Project project) {
                this.project = project;
            }

            @Override
            public Iterable<GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec> transform(Set<GradlePluginTestingStrategy> strategies) {
                Set<GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec> result = new LinkedHashSet<>();
                if (strategies.isEmpty()) {
                    TaskProvider<Test> testTask = createTestTask(testSuite);
                    testTask.configure(new RegisterTestingStrategyPropertyExtensionRule(project.getObjects()));
                    result.add(new GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec(testTask));
                } else if (strategies.size() == 1) {
                    TaskProvider<Test> testTask = createTestTask(testSuite);
                    testTask.configure(new RegisterTestingStrategyPropertyExtensionRule(project.getObjects()));
                    testTask.configure(configureTestingStrategy(testSuite, strategies.iterator().next()));
                    result.add(new GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec(testTask));
                } else {
                    for (GradlePluginTestingStrategy strategy : strategies) {
                        TaskProvider<Test> testTask = createTestTask(testSuite, strategy.getName());
                        testTask.configure(new RegisterTestingStrategyPropertyExtensionRule(project.getObjects()));
                        testTask.configure(configureTestingStrategy(testSuite, strategy));
                        result.add(new GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec(testTask));
                    }
                }

                return result;
            }

            private Action<Test> configureTestingStrategy(GradlePluginDevelopmentTestSuite testSuite, GradlePluginTestingStrategy strategy) {
                return task -> {
                    Stream.of(strategy)
                            .flatMap(this::unpackCompositeTestingStrategy)
                            .flatMap(this::onlyCoverageTestingStrategy)
                            .map(GradleVersionCoverageTestingStrategy::getVersion)
                            .findFirst()
                            .ifPresent(setDefaultGradleVersionSystemProperty(task));
                    testingStrategyProperty(task).set(strategy);
                };
            }

            private Consumer<String> setDefaultGradleVersionSystemProperty(Test task) {
                return version -> task.systemProperty("dev.gradleplugins.defaultGradleVersion", version);
            }

            private Stream<GradleVersionCoverageTestingStrategy> onlyCoverageTestingStrategy(GradlePluginTestingStrategy strategy) {
                if (strategy instanceof GradleVersionCoverageTestingStrategy) {
                    return Stream.of((GradleVersionCoverageTestingStrategy) strategy);
                } else {
                    return Stream.empty();
                }
            }

            private Stream<GradlePluginTestingStrategy> unpackCompositeTestingStrategy(GradlePluginTestingStrategy strategy) {
                if (strategy instanceof CompositeGradlePluginTestingStrategy) {
                    return StreamSupport.stream(((CompositeGradlePluginTestingStrategy) strategy).spliterator(), false);
                } else {
                    return Stream.of(strategy);
                }
            }

            private TaskProvider<Test> createTestTask(GradlePluginDevelopmentTestSuite testSuite) {
                return createTestTask(testSuite, "");
            }

            private TaskProvider<Test> createTestTask(GradlePluginDevelopmentTestSuite testSuite, String variant) {
                String taskName = testSuite.getName() + StringUtils.capitalize(variant);

                TaskProvider<Test> result = null;
                if (project.getTasks().getNames().contains(taskName)) {
                    result = project.getTasks().named(taskName, Test.class);
                } else {
                    result = project.getTasks().register(taskName, Test.class);
                }

                result.configure(it -> {
                    it.setDescription("Runs the " + GUtil.toWords(testSuite.getName()) + "s.");
                    it.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);

                    it.setTestClassesDirs(project.getObjects().fileCollection().from(testSuite.getSourceSet().map(t -> (Object) t.getOutput().getClassesDirs()).orElse(emptyList())));
                    it.setClasspath(project.getObjects().fileCollection().from(testSuite.getSourceSet().map(t -> (Object) t.getRuntimeClasspath()).orElse(emptyList())));
                });

                return result;
            }
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

        @Override
        public String toString() {
            return "test suite '" + name + "'";
        }

        @Override
        public GradlePluginDevelopmentTestSuiteInternal.TestTaskView getTestTasks() {
            return testTasks;
        }

        public abstract SetProperty<GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec> getTestSpecs();

        @Override
        public TaskProvider<PluginUnderTestMetadata> getPluginUnderTestMetadataTask() {
            return pluginUnderTestMetadataTask;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        public static /*final*/ abstract class TestTaskView implements TaskView<Test> {
            private final DomainObjectSet<GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec> testTaskSpecs;
            private final Provider<Set<Test>> elementsProvider;

            @Inject
            public TestTaskView(Project project, Provider<Set<GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec>> testSpecsProvider) {
                this.testTaskSpecs = project.getObjects().domainObjectSet(GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec.class);
                this.elementsProvider = project.provider(() -> testTaskSpecs.stream().map(GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec::get).collect(Collectors.toCollection(LinkedHashSet::new)));

                testTaskSpecs.addAllLater(testSpecsProvider);
            }

            public boolean add(TaskProvider<Test> taskProvider) {
                return testTaskSpecs.add(new GradlePluginDevelopmentTestSuiteInternal.TestTaskView.Spec(taskProvider));
            }

            @Override
            public void configureEach(Action<? super Test> action) {
                testTaskSpecs.configureEach(spec -> spec.configure(action));
            }

            @Override
            public Provider<Set<Test>> getElements() {
                return elementsProvider;
            }

            public void realize() {
                testTaskSpecs.iterator();
            }

            private static final class Spec {
                private final TaskProvider<Test> taskProvider;

                public Spec(TaskProvider<Test> taskProvider) {
                    this.taskProvider = taskProvider;
                }

                public void configure(Action<? super Test> action) {
                    taskProvider.configure(action);
                }

                public Test get() {
                    return taskProvider.get();
                }
            }
        }

        @Override
        public GradlePluginDevelopmentTestSuiteDependencies getDependencies() {
            return dependencies;
        }

        @Override
        public void dependencies(Action<? super GradlePluginDevelopmentTestSuiteDependencies> action) {
            action.execute(dependencies);
        }
    }
}
