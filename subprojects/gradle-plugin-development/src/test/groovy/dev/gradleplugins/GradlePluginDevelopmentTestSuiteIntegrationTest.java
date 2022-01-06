package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class GradlePluginDevelopmentTestSuiteIntegrationTest implements GradlePluginDevelopmentTestSuiteTester {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = GradlePluginDevelopmentTestSuiteFactory.forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("gote");

    @Override
    public GradlePluginDevelopmentTestSuite subject() {
        return subject;
    }

    @Test
    void hasToString() {
        assertThat(subject(), Matchers.hasToString("test suite 'gote'"));
    }
}
