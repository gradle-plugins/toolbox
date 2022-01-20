package dev.gradleplugins.internal;

import dev.gradleplugins.*;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.internal.Actions;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static dev.gradleplugins.internal.DefaultDependencyVersions.SPOCK_FRAMEWORK_VERSION;

public abstract class GradlePluginDevelopmentTestSuiteInternal implements GradlePluginDevelopmentTestSuite, SoftwareComponent, HasPublicType, FinalizableComponent {
    private final GradlePluginTestingStrategyFactory strategyFactory;
    private final Dependencies dependencies;
    private final String name;
    @Getter private final List<Action<? super Test>> testTaskActions = new ArrayList<>();
    private final Action<GradlePluginDevelopmentTestSuiteInternal> finalizeAction;
    private final TestTaskView testTasks;
    private boolean finalized = false;

    @Inject
    public GradlePluginDevelopmentTestSuiteInternal(String name, TaskContainer tasks, ObjectFactory objects, PluginManager pluginManager, ProviderFactory providers, Provider<String> minimumGradleVersion) {
        this.strategyFactory = new GradlePluginTestingStrategyFactoryInternal(minimumGradleVersion);
        this.name = name;
        this.dependencies = objects.newInstance(Dependencies.class, getSourceSet(), pluginManager, minimumGradleVersion.orElse(GradleVersion.current().getVersion()).map(GradleRuntimeCompatibility::groovyVersionOf));
        tasks.withType(PluginUnderTestMetadata.class).configureEach(task -> {
            if (task.getName().equals("pluginUnderTestMetadata")) {
                task.getPluginClasspath().from((Callable<Object>) dependencies::pluginUnderTestMetadata);
            }
        });
        this.testTasks = objects.newInstance(TestTaskView.class, testTaskActions, providers.provider(new FinalizeComponentCallable<>()).orElse(getTestTaskCollection()));
        this.finalizeAction = Actions.composite(new TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule(), new CreateTestTasksFromTestingStrategiesRule(tasks, objects, getTestTaskCollection()), new AttachTestTasksToCheckTaskIfPresent(pluginManager, tasks), new FinalizeTestSuiteProperties());
        getSourceSet().finalizeValueOnRead();
        getTestingStrategies().finalizeValueOnRead();
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
            finalizeAction.execute(this);
            getSourceSet().finalizeValue();
        }
    }

    @Override
    public boolean isFinalized() {
        return finalized;
    }

    @Override
    public Dependencies getDependencies() {
        return dependencies;
    }

    @Override
    public void dependencies(Action<? super GradlePluginDevelopmentTestSuiteDependencies> action) {
        action.execute(dependencies);
    }

    protected abstract static class Dependencies implements GradlePluginDevelopmentTestSuiteDependencies {
        private final Provider<SourceSet> sourceSetProvider;
        private final PluginManager pluginManager;
        private final Provider<String> defaultGroovyVersion;

        @Inject
        protected abstract ConfigurationContainer getConfigurations();

        @Inject
        protected abstract DependencyHandler getDependencies();

        private SourceSet sourceSet() {
            return sourceSetProvider.get();
        }

        private Configuration pluginUnderTestMetadata() {
            return getConfigurations().maybeCreate(sourceSet().getName() + "PluginUnderTestMetadata");
        }

        @Inject
        public Dependencies(Provider<SourceSet> sourceSetProvider, PluginManager pluginManager, Provider<String> defaultGroovyVersion) {
            this.sourceSetProvider = sourceSetProvider;
            this.pluginManager = pluginManager;
            this.defaultGroovyVersion = defaultGroovyVersion;
        }

        @Override
        public void implementation(Object notation) {
            GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies()).add(sourceSet().getImplementationConfigurationName(), notation);
        }

        @Override
        public void implementation(Object notation, Action<? super ModuleDependency> action) {
            ModuleDependency dependency = (ModuleDependency) getDependencies().create(notation);
            action.execute(dependency);
            getDependencies().add(sourceSet().getImplementationConfigurationName(), dependency);
        }

        @Override
        public void compileOnly(Object notation) {
            GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies()).add(sourceSet().getCompileOnlyConfigurationName(), notation);
        }

        @Override
        public void runtimeOnly(Object notation) {
            GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies()).add(sourceSet().getRuntimeOnlyConfigurationName(), notation);
        }

        @Override
        public void annotationProcessor(Object notation) {
            getDependencies().add(sourceSet().getAnnotationProcessorConfigurationName(), notation);
        }

        @Override
        public void pluginUnderTestMetadata(Object notation) {
            getDependencies().add(pluginUnderTestMetadata().getName(), notation);
        }

        @Override
        public Object testFixtures(Object notation) {
            return getDependencies().testFixtures(notation);
        }

        @Override
        public Object platform(Object notation) {
            return getDependencies().platform(notation);
        }

        @Override
        public Object spockFramework() {
            return spockFramework(SPOCK_FRAMEWORK_VERSION);
        }

        @Override
        public Object spockFramework(String version) {
            pluginManager.apply("groovy-base"); // Spock framework imply Groovy implementation language
            return GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies()).spockFramework(version);
        }

        @Override
        public Object gradleFixtures() {
            return GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies()).gradleFixtures();
        }

        @Override
        public Object gradleTestKit() {
            return getDependencies().gradleTestKit();
        }

        @Override
        public Object groovy() {
            return defaultGroovyVersion.map(GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies())::groovy);
        }

        @Override
        public Object groovy(String version) {
            return GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies()).groovy(version);
        }

        @Override
        public Object gradleApi(String version) {
            return GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies()).gradleApi(version);
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
