package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;

class GradlePluginDevelopmentTestSuiteJavaGradlePluginPluginAppliedIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("jike");

    @BeforeEach
    void applyJavaGradlePluginPlugin() {
        project.getPluginManager().apply("java-gradle-plugin");
    }

    @Test
    void hasMainSourceSetAsTestedSourceSetConvention() {
        assertThat(subject.getTestedSourceSet().value((SourceSet) null), providerOf(named("main")));
    }

    @Test
    void usesDevelPluginSourceSetAsTestedSourceSetConvention() {
        gradlePlugin(project).pluginSourceSet(sourceSets(project).create("anotherMain"));
        assertThat(subject.getTestedSourceSet().value((SourceSet) null), providerOf(named("anotherMain")));
    }
}
