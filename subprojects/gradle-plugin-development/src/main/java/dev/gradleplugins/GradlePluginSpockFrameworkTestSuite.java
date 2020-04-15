package dev.gradleplugins;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;

public interface GradlePluginSpockFrameworkTestSuite {
    /**
     * Configure the testing strategy for this test suite.
     *
     * @return a property for configuring the {@link GradlePluginTestingStrategy}
     */
    Property<GradlePluginTestingStrategy> getTestingStrategy();

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
}
