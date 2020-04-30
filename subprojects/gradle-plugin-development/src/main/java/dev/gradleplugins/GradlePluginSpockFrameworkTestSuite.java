package dev.gradleplugins;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;

public interface GradlePluginSpockFrameworkTestSuite {
    /**
     * Configure the testing strategies for this test suite.
     *
     * @return a property for configuring the {@link GradlePluginTestingStrategy}
     */
    SetProperty<GradlePluginTestingStrategy> getTestingStrategies();

    /**
     * Configure the Gradle plugin source set to test by this test suite.
     *
     * @return a property for configuring the {@link SourceSet}
     */
    Property<SourceSet> getTestedSourceSet();

    /**
     * Configure the Spock framework version to use by this test suite.
     *
     * @return a property for configuring the Spock framework version
     */
    Property<String> getSpockVersion();

    /**
     * Returns a factory for creating the various testing strategies.
     *
     * @return a {@link GradlePluginTestingStrategyFactory} instance, never null.
     */
    GradlePluginTestingStrategyFactory getStrategies();

    TaskView<Test> getTestTasks();
}
