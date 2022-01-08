package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;

public interface GradlePluginDevelopmentTestSuite extends Named {
    /**
     * Configure the testing strategies for this test suite.
     *
     * @return a property for configuring the {@link GradlePluginTestingStrategy}
     */
    SetProperty<GradlePluginTestingStrategy> getTestingStrategies();

    /**
     * Configure the test suite source set.
     *
     * @return a property for configuring the {@link SourceSet}, never null
     */
    Property<SourceSet> getSourceSet();

    /**
     * Configure the Gradle plugin source set to test by this test suite.
     *
     * @return a property for configuring the {@link SourceSet}
     */
    Property<SourceSet> getTestedSourceSet();

    /**
     * Returns a factory for creating the various testing strategies.
     *
     * @return a {@link GradlePluginTestingStrategyFactory} instance, never null.
     */
    GradlePluginTestingStrategyFactory getStrategies();

    TaskView<Test> getTestTasks();

    GradlePluginDevelopmentTestSuiteDependencies getDependencies();

    void dependencies(Action<? super GradlePluginDevelopmentTestSuiteDependencies> action);

    /**
     * Finalize this component.
     * Upon finalizing, the test suite will calculate its final testing strategies and create all of its test tasks.
     * Once the component is finalized, users cannot change any of component properties.
     */
    void finalizeComponent();
}
