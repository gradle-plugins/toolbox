package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket;
import dev.gradleplugins.GradlePluginDevelopmentDependencyModifiers;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteDependencies;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import dev.gradleplugins.internal.util.GradleRuntimeCompatibility;
import dev.gradleplugins.TaskView;
import dev.gradleplugins.internal.runtime.dsl.GroovyHelper;
import dev.gradleplugins.internal.util.LocalOrRemoteVersionTransformer;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.gradleplugins.internal.DefaultDependencyVersions.SPOCK_FRAMEWORK_VERSION;
import static java.lang.String.format;

public abstract class GradlePluginDevelopmentTestSuiteInternal implements GradlePluginDevelopmentTestSuite, SoftwareComponent, HasPublicType, FinalizableComponent {
    private static final String PLUGIN_UNDER_TEST_METADATA_TASK_NAME_PREFIX = "pluginUnderTestMetadata";
    private static final String PLUGIN_DEVELOPMENT_GROUP = "Plugin development";
    private static final String PLUGIN_UNDER_TEST_METADATA_TASK_DESCRIPTION_FORMAT = "Generates the metadata for plugin %s.";
    private final GradlePluginTestingStrategyFactory strategyFactory;
    private final Dependencies dependencies;
    private final String name;
    private final List<Action<? super Test>> testTaskActions = new ArrayList<>();
    private final List<Action<? super GradlePluginDevelopmentTestSuite>> finalizeActions = new ArrayList<>();
    private final TestTaskView testTasks;
    private final TaskProvider<PluginUnderTestMetadata> pluginUnderTestMetadataTask;
    private final String displayName;
    private boolean finalized = false;

    @Inject
    public GradlePluginDevelopmentTestSuiteInternal(String name, Project project, TaskContainer tasks, ObjectFactory objects, PluginManager pluginManager, ProviderFactory providers, Provider<String> minimumGradleVersion, ReleasedVersionDistributions releasedVersions) {
        this.strategyFactory = new GradlePluginTestingStrategyFactoryInternal(minimumGradleVersion, releasedVersions);
        this.name = name;
        this.displayName = GUtil.toWords(name) + "s";
        this.dependencies = objects.newInstance(Dependencies.class, project, minimumGradleVersion.orElse(GradleVersion.current().getVersion()).map(GradleRuntimeCompatibility::groovyVersionOf), this);

        // adhoc decoration of the dependencies
        dependencies.forEach(dependencyBucket -> {
            GroovyHelper.instance().addNewInstanceMethod(dependencies, dependencyBucket.getName(), new MethodClosure(dependencyBucket, "add"));
        });
        GroovyHelper.instance().addNewInstanceMethod(dependencies, "platform", new MethodClosure(dependencies.getPlatform(), "modify"));
        GroovyHelper.instance().addNewInstanceMethod(dependencies, "enforcedPlatform", new MethodClosure(dependencies.getEnforcedPlatform(), "modify"));
        GroovyHelper.instance().addNewInstanceMethod(dependencies, "testFixtures", new MethodClosure(dependencies.getTestFixtures(), "modify"));

        this.pluginUnderTestMetadataTask = registerPluginUnderTestMetadataTask(tasks, pluginUnderTestMetadataTaskName(name), displayName);
        this.testTasks = objects.newInstance(TestTaskView.class, testTaskActions, providers.provider(new FinalizeComponentCallable<>()).orElse(getTestTaskCollection()));
        this.finalizeActions.add(new TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule());
        this.finalizeActions.add(new CreateTestTasksFromTestingStrategiesRule(tasks, objects, getTestTaskCollection()));
        this.finalizeActions.add(new AttachTestTasksToCheckTaskIfPresent(pluginManager, tasks));
        this.finalizeActions.add(new FinalizeTestSuiteProperties());
        getSourceSet().finalizeValueOnRead();
        getTestingStrategies().finalizeValueOnRead();
    }

    public List<Action<? super Test>> getTestTaskActions() {
        return testTaskActions;
    }

    // From Gradle codebase
    private static final class GUtil {
        private static final Pattern UPPER_LOWER = Pattern.compile("(?m)([A-Z]*)([a-z0-9]*)");

        public static String toWords(CharSequence string) {
            return toWords(string, ' ');
        }

        public static String toWords(CharSequence string, char separator) {
            if (string == null) {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            int pos = 0;
            Matcher matcher = UPPER_LOWER.matcher(string);
            while (pos < string.length()) {
                matcher.find(pos);
                if (matcher.end() == pos) {
                    // Not looking at a match
                    pos++;
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append(separator);
                }
                String group1 = matcher.group(1).toLowerCase();
                String group2 = matcher.group(2);
                if (group2.length() == 0) {
                    builder.append(group1);
                } else {
                    if (group1.length() > 1) {
                        builder.append(group1.substring(0, group1.length() - 1));
                        builder.append(separator);
                        builder.append(group1.substring(group1.length() - 1));
                    } else {
                        builder.append(group1);
                    }
                    builder.append(group2);
                }
                pos = matcher.end();
            }

            return builder.toString();
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

    protected abstract static class Dependencies implements GradlePluginDevelopmentTestSuiteDependencies, Iterable<GradlePluginDevelopmentDependencyBucket> {
        private final PluginManager pluginManager;
        private final Provider<String> defaultGroovyVersion;
        private final DependencyFactory dependencyFactory;
        private final Supplier<NamedDomainObjectProvider<Configuration>> pluginUnderTestMetadataSupplier;
        private final Project project;
        private final Map<String, GradlePluginDevelopmentDependencyBucket> dependencyBuckets = new LinkedHashMap<>();
        private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier platformDependencyModifier;
        private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier enforcedPlatformDependencyModifier;
        private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier testFixturesDependencyModifier;
        private final Transformer<Dependency, String> localOrRemoteGradleTestKit;
        private final Transformer<Dependency, String> localOrRemoteGradleApi;

        private NamedDomainObjectProvider<Configuration> pluginUnderTestMetadata() {
            return pluginUnderTestMetadataSupplier.get();
        }

        @Inject
        public Dependencies(Project project, Provider<String> defaultGroovyVersion, GradlePluginDevelopmentTestSuite testSuite) {
            this.pluginManager = project.getPluginManager();
            this.defaultGroovyVersion = defaultGroovyVersion;
            this.dependencyFactory = DependencyFactory.forProject(project);
            this.localOrRemoteGradleTestKit = new LocalOrRemoteVersionTransformer<>(dependencyFactory::localGradleTestKit, dependencyFactory::gradleTestKit);
            this.localOrRemoteGradleApi = new LocalOrRemoteVersionTransformer<>(dependencyFactory::localGradleApi, dependencyFactory::gradleApi);
            this.pluginUnderTestMetadataSupplier = new PluginUnderTestMetadataConfigurationSupplier(project, testSuite);
            project.afterEvaluate(__ -> pluginUnderTestMetadataSupplier.get()); // for now
            DependencyBucketFactory bucketFactory = new DependencyBucketFactory(project, testSuite.getSourceSet());
            this.platformDependencyModifier = new PlatformDependencyModifier(project);
            this.enforcedPlatformDependencyModifier = new EnforcedPlatformDependencyModifier(project);
            this.testFixturesDependencyModifier = new TestFixturesDependencyModifier(project);
            this.project = project;
            add(bucketFactory.create("implementation"));
            add(bucketFactory.create("compileOnly"));
            add(bucketFactory.create("runtimeOnly"));
            add(bucketFactory.create("annotationProcessor"));
            add(bucketFactory.create("pluginUnderTestMetadata"));
            add(bucketFactory.create("pluginUnderTest"));
        }

        private void add(GradlePluginDevelopmentDependencyBucket dependencyBucket) {
            dependencyBuckets.put(dependencyBucket.getName(), dependencyBucket);
        }

        @Override
        public void implementation(Object notation) {
            addDependency(getImplementation(), notation);
        }

        @Override
        public void implementation(Object notation, Action<? super ModuleDependency> action) {
            getImplementation().add((ModuleDependency) dependencyFactory.create(notation), action);
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getImplementation() {
            return dependencyBuckets.get("implementation");
        }

        @Override
        public void compileOnly(Object notation) {
            addDependency(getCompileOnly(), notation);
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getCompileOnly() {
            return dependencyBuckets.get("compileOnly");
        }

        @Override
        public void runtimeOnly(Object notation) {
            addDependency(getRuntimeOnly(), notation);
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getRuntimeOnly() {
            return dependencyBuckets.get("runtimeOnly");
        }

        @Override
        public void annotationProcessor(Object notation) {
            addDependency(getAnnotationProcessor(), notation);
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getAnnotationProcessor() {
            return dependencyBuckets.get("annotationProcessor");
        }

        @Override
        public void pluginUnderTestMetadata(Object notation) {
            addDependency(dependencyBuckets.get("pluginUnderTestMetadata"), notation);
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getPluginUnderTest() {
            return dependencyBuckets.get("pluginUnderTest");
        }

        private void addDependency(GradlePluginDevelopmentDependencyBucket bucket, Object notation) {
            if (notation instanceof Provider) {
                bucket.add(((Provider<?>) notation).map(dependencyFactory::create));
            } else {
                bucket.add(dependencyFactory.create(notation));
            }
        }

        @Override
        public NamedDomainObjectProvider<Configuration> getPluginUnderTestMetadata() {
            return pluginUnderTestMetadata();
        }

        @Override
        public ModuleDependency testFixtures(Object notation) {
            if (notation instanceof CharSequence) {
                return getTestFixtures().modify((CharSequence) notation);
            } else if (notation instanceof ModuleDependency) {
                return getTestFixtures().modify((ModuleDependency) notation);
            } else if (notation instanceof Project) {
                return getTestFixtures().modify(project);
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public GradlePluginDevelopmentDependencyModifiers.DependencyModifier getTestFixtures() {
            return testFixturesDependencyModifier;
        }

        @Override
        public ModuleDependency platform(Object notation) {
            if (notation instanceof CharSequence) {
                return getPlatform().modify((CharSequence) notation);
            } else if (notation instanceof ModuleDependency) {
                return getPlatform().modify((ModuleDependency) notation);
            } else if (notation instanceof Project) {
                return getPlatform().modify(project);
            }
            throw new UnsupportedOperationException();
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
        public ProjectDependency project(String projectPath) {
            return dependencyFactory.create(project.project(projectPath));
        }

        @Override
        public ProjectDependency project() {
            return dependencyFactory.create(project);
        }

        @Override
        public Object spockFramework() {
            return spockFramework(SPOCK_FRAMEWORK_VERSION);
        }

        @Override
        public Object spockFramework(String version) {
            pluginManager.apply("groovy-base"); // Spock framework imply Groovy implementation language
            return dependencyFactory.spockFramework(version);
        }

        @Override
        public Object gradleFixtures() {
            return dependencyFactory.gradleFixtures();
        }

        @Override
        public Object gradleTestKit() {
            return dependencyFactory.localGradleTestKit();
        }

        @Override
        public Object gradleTestKit(String version) {
            return localOrRemoteGradleTestKit.transform(version);
        }

        @Override
        public Object groovy() {
            return defaultGroovyVersion.map(dependencyFactory::groovy);
        }

        @Override
        public Object groovy(String version) {
            return dependencyFactory.groovy(version);
        }

        @Override
        public Object gradleApi(String version) {
            return localOrRemoteGradleApi.transform(version);
        }

        @Override
        public Iterator<GradlePluginDevelopmentDependencyBucket> iterator() {
            return dependencyBuckets.values().iterator();
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
