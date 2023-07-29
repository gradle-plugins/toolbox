package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;

public interface GradlePluginDevelopmentTestSuite extends Named, ExtensionAware {
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

    /**
     * Returns plugin under test metadata generator task for this test suite.
     *
     * Use {@link GradlePluginDevelopmentTestSuiteDependencies#getPluginUnderTestMetadata()} to add plugin classpath.
     *
     * @return generator task for {@literal plugin-under-test-metadata.properties} file, never null
     */
    TaskProvider<PluginUnderTestMetadata> getPluginUnderTestMetadataTask();

    /**
     * Returns a human-readable name for this test suite.
     *
     * @return a human-readable name, never null
     */
    String getDisplayName();

    TaskView<Test> getTestTasks();

    GradlePluginDevelopmentTestSuiteDependencies getDependencies();

    void dependencies(Action<? super GradlePluginDevelopmentTestSuiteDependencies> action);
}
