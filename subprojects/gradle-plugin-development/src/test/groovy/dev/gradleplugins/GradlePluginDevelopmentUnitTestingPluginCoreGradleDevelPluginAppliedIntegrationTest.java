package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.internal.plugins.GradlePluginDevelopmentUnitTestingPlugin.test;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

class GradlePluginDevelopmentUnitTestingPluginCoreGradleDevelPluginAppliedIntegrationTest implements GradlePluginDevelopmentTestSuiteCoreGradleDevelPluginAppliedIntegrationTester {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void appliesSubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-unit-test");
    }

    @Override
    public GradlePluginDevelopmentTestSuite subject() {
        return test(project);
    }

    @Override
    public Project project() {
        return project;
    }

    @Test
    void includesSourceSetInDevelTestSourceSets() {
        assertThat(gradlePlugin(project).getTestSourceSets(), not(hasItem(named("test"))));
    }

    @Test
    void hasGradleApiImplementationDependency() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
        compatibility(gradlePlugin(project)).getMinimumGradleVersion().set("5.6");
        assertThat(project.getConfigurations().getByName("testImplementation").getDependencies(), hasItem(coordinate("dev.gradleplugins:gradle-api:5.6")));
    }
}
