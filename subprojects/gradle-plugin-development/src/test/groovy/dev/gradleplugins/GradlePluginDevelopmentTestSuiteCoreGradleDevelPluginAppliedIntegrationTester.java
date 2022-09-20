package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;

public interface GradlePluginDevelopmentTestSuiteCoreGradleDevelPluginAppliedIntegrationTester {
    GradlePluginDevelopmentTestSuite subject();
    Project project();

    @BeforeEach
    default void applyJavaGradlePluginPlugin() {
        project().getPluginManager().apply("java-gradle-plugin");
    }

    @Test
    default void hasMainSourceSetAsTestedSourceSetConvention() {
        assertThat(subject().getTestedSourceSet().value((SourceSet) null), providerOf(named("main")));
    }

    @Test
    default void usesDevelPluginSourceSetAsTestedSourceSetConvention() {
        gradlePlugin(project()).pluginSourceSet(sourceSets(project()).map(it -> it.maybeCreate("anotherMain")).get());
        assertThat(subject().getTestedSourceSet().value((SourceSet) null), providerOf(named("anotherMain")));
    }
}
