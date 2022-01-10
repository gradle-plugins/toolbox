package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.internal.plugins.GradlePluginDevelopmentFunctionalTestingPlugin.functionalTest;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class GradlePluginDevelopmentFunctionalTestingPluginCoreGradleDevelPluginAppliedIntegrationTest implements GradlePluginDevelopmentTestSuiteCoreGradleDevelPluginAppliedIntegrationTester {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void appliesSubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-functional-test");
    }

    @Override
    public GradlePluginDevelopmentTestSuite subject() {
        return functionalTest(project);
    }

    @Override
    public Project project() {
        return project;
    }

    @Test
    void includesSourceSetInDevelTestSourceSets() {
        assertThat(gradlePlugin(project).getTestSourceSets(), hasItem(named("functionalTest")));
    }
}
