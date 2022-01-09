package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class GradlePluginDevelopmentTestSuiteTestViewRealizedIntegrationTest implements GradlePluginDevelopmentTestSuiteFinalizedIntegrationTester {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("lokk");

    @BeforeEach
    void realizesTaskView() {
        project.getPluginManager().apply("java-base");
        subject.getTestingStrategies().set(asList(subject.getStrategies().coverageForGradleVersion("6.8"), subject.getStrategies().coverageForGradleVersion("7.1")));
        subject.getTestTasks().getElements().get();
    }

    public GradlePluginDevelopmentTestSuite subject() {
        return subject;
    }

    @Test
    void returnsTestTasksOnTaskViewElementQuery() {
        assertThat(subject.getTestTasks().getElements(), providerOf(contains(named("lokk6.8"), named("lokk7.1"))));
    }
}
