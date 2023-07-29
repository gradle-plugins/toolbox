package dev.gradleplugins;

import dev.gradleplugins.internal.DefaultGradlePluginDevelopmentTestSuiteFactory;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;

class GradlePluginDevelopmentTestSuiteFactoryIntegrationTest {
    Project project = ProjectBuilder.builder().build();
    GradlePluginDevelopmentTestSuiteFactory subject = new DefaultGradlePluginDevelopmentTestSuiteFactory(project);

    @Test
    void createsTestSuiteUsingName() {
        assertThat(subject.create("kote"), named("kote"));
    }
}
