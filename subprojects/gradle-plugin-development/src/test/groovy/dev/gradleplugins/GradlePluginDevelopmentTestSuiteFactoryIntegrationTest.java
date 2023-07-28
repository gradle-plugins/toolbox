package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;

class GradlePluginDevelopmentTestSuiteFactoryIntegrationTest {
    Project project = ProjectBuilder.builder().build();
    GradlePluginDevelopmentTestSuiteFactory subject;

    @BeforeEach
    void givenProject() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        subject = forProject(project);
    }

    @Test
    void createsTestSuiteUsingName() {
        assertThat(subject.create("kote"), named("kote"));
    }
}
