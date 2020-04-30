package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginSpockFrameworkTestSuite;
import dev.gradleplugins.GradlePluginSpockFrameworkTestSuiteDependencies;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import dev.gradleplugins.TaskView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.testing.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class GradlePluginSpockFrameworkTestSuiteInternal implements GradlePluginSpockFrameworkTestSuite, SoftwareComponent {
    private final GradlePluginTestingStrategyFactory strategyFactory = getObjects().newInstance(GradlePluginTestingStrategyFactoryInternal.class);
    @Getter private final GradlePluginSpockFrameworkTestSuiteDependencies dependencies;
    @Getter private final String name;
    @Getter private final SourceSet sourceSet;
    @Getter private final List<Action<? super Test>> testTaskActions = new ArrayList<>();

    @Inject
    protected abstract ObjectFactory getObjects();

    @Inject
    protected abstract TaskContainer getTasks();

    @Inject
    public GradlePluginSpockFrameworkTestSuiteInternal(String name, SourceSet sourceSet) {
        this.name = name;
        this.sourceSet = sourceSet;
        this.dependencies = getObjects().newInstance(Dependencies.class, sourceSet);
    }

    @Override
    public GradlePluginTestingStrategyFactory getStrategies() {
        return strategyFactory;
    }

    public abstract Property<GradlePluginDevelopmentExtensionInternal> getTestedGradlePlugin();

    @Override
    public TaskView<Test> getTestTasks() {
        return getObjects().newInstance(TestTaskView.class, testTaskActions);
    }

    @RequiredArgsConstructor(onConstructor_={@Inject})
    protected static class TestTaskView implements TaskView<Test> {
        private final List<Action<? super Test>> testTaskActions;

        @Override
        public void configureEach(Action<? super Test> action) {
            testTaskActions.add(action);
        }
    }

    @Override
    public void dependencies(Action<? super GradlePluginSpockFrameworkTestSuiteDependencies> action) {
        action.execute(dependencies);
    }

    @RequiredArgsConstructor(onConstructor_={@Inject})
    protected abstract static class Dependencies implements GradlePluginSpockFrameworkTestSuiteDependencies {
        private final SourceSet sourceSet;

        @Inject
        protected abstract DependencyHandler getDependencies();

        @Override
        public void implementation(Object notation) {
            getDependencies().add(sourceSet.getImplementationConfigurationName(), notation);
        }

        @Override
        public void implementation(Object notation, Action<? super ExternalModuleDependency> action) {
            ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencies().create(notation);
            action.execute(dependency);
            getDependencies().add(sourceSet.getImplementationConfigurationName(), dependency);
        }

        @Override
        public void compileOnly(Object notation) {
            getDependencies().add(sourceSet.getCompileOnlyConfigurationName(), notation);
        }

        @Override
        public void annotationProcessor(Object notation) {
            getDependencies().add(sourceSet.getAnnotationProcessorConfigurationName(), notation);
        }
    }
}
