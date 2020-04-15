package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginSpockFrameworkTestSuite;
import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public abstract class GradlePluginSpockFrameworkTestSuiteInternal extends GroovySpockFrameworkTestSuite implements GradlePluginSpockFrameworkTestSuite {
    @Inject
    public GradlePluginSpockFrameworkTestSuiteInternal(String name, SourceSet sourceSet, TaskContainer tasks) {
        super(name, sourceSet, tasks);
        getTestingStrategy().convention(GradlePluginTestingStrategyFactory.none());
    }

    public abstract Property<GradlePluginTestingStrategy> getTestingStrategy();

    public abstract Property<GradlePluginDevelopmentExtensionInternal> getTestedGradlePlugin();
}
