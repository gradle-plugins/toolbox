package dev.gradleplugins.internal;

import dev.gradleplugins.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static dev.gradleplugins.internal.DefaultDependencyVersions.SPOCK_FRAMEWORK_VERSION;

public abstract class GradlePluginDevelopmentTestSuiteInternal implements GradlePluginDevelopmentTestSuite, SoftwareComponent {
    private final GradlePluginTestingStrategyFactory strategyFactory = getObjects().newInstance(GradlePluginTestingStrategyFactoryInternal.class);
    @Getter private final Dependencies dependencies;
    @Getter private final String name;
    @Getter private final SourceSet sourceSet;
    @Getter private final List<Action<? super Test>> testTaskActions = new ArrayList<>();

    @Inject
    protected abstract ObjectFactory getObjects();

    @Inject
    protected abstract TaskContainer getTasks();

    @Inject
    public GradlePluginDevelopmentTestSuiteInternal(String name, SourceSet sourceSet, PluginManager pluginManager) {
        this.name = name;
        this.sourceSet = sourceSet;
        this.dependencies = getObjects().newInstance(Dependencies.class, sourceSet, pluginManager, getTestedGradlePlugin().flatMap(GradlePluginDevelopmentCompatibilityExtension::getMinimumGradleVersion).map(GradleRuntimeCompatibility::groovyVersionOf));
        getTasks().named("pluginUnderTestMetadata", PluginUnderTestMetadata.class, task -> {
            task.getPluginClasspath().from(dependencies.pluginUnderTestMetadata);
        });
    }

    @Override
    public GradlePluginTestingStrategyFactory getStrategies() {
        return strategyFactory;
    }

    public abstract Property<GradlePluginDevelopmentCompatibilityExtension> getTestedGradlePlugin();

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
    public void dependencies(Action<? super GradlePluginDevelopmentTestSuiteDependencies> action) {
        action.execute(dependencies);
    }

    protected abstract static class Dependencies implements GradlePluginDevelopmentTestSuiteDependencies {
        private final SourceSet sourceSet;
        private final PluginManager pluginManager;
        private final Provider<String> defaultGroovyVersion;
        private final Configuration pluginUnderTestMetadata;

        @Inject
        protected abstract ConfigurationContainer getConfigurations();

        @Inject
        protected abstract DependencyHandler getDependencies();

        @Inject
        public Dependencies(SourceSet sourceSet, PluginManager pluginManager, Provider<String> defaultGroovyVersion) {
            this.sourceSet = sourceSet;
            this.pluginManager = pluginManager;
            this.defaultGroovyVersion = defaultGroovyVersion;
            this.pluginUnderTestMetadata = getConfigurations().create(sourceSet.getName() + StringUtils.capitalize("pluginUnderTestMetadata"));
        }

        @Override
        public void implementation(Object notation) {
            GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies()).add(sourceSet.getImplementationConfigurationName(), notation);
        }

        @Override
        public void implementation(Object notation, Action<? super ModuleDependency> action) {
            ModuleDependency dependency = (ModuleDependency) getDependencies().create(notation);
            action.execute(dependency);
            getDependencies().add(sourceSet.getImplementationConfigurationName(), dependency);
        }

        @Override
        public void compileOnly(Object notation) {
            GradlePluginDevelopmentDependencyExtensionInternal.of(getDependencies()).add(sourceSet.getCompileOnlyConfigurationName(), notation);
        }

        @Override
        public void annotationProcessor(Object notation) {
            getDependencies().add(sourceSet.getAnnotationProcessorConfigurationName(), notation);
        }

        @Override
        public void pluginUnderTestMetadata(Object notation) {
            getDependencies().add(pluginUnderTestMetadata.getName(), notation);
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
    }
}
