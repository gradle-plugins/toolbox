package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.*;
import static dev.gradleplugins.internal.plugins.GradlePluginDevelopmentFunctionalTestingPlugin.functionalTest;
import static dev.gradleplugins.internal.plugins.GradlePluginDevelopmentUnitTestingPlugin.test;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class GradlePluginDevelopmentFunctionalTestingPluginUnitTestingPluginAppliedIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void appliesSubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-functional-test");
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-unit-test");
        final GradlePluginTestingStrategyFactory strategies = functionalTest(project).getStrategies();
        functionalTest(project).getTestingStrategies().set(asList(strategies.coverageForGradleVersion("6.3"), strategies.coverageForGradleVersion("7.2")));
        test(project).getTestingStrategies().set(asList(strategies.coverageForGradleVersion("6.7"), strategies.coverageForGradleVersion("7.0")));
    }

    public GradlePluginDevelopmentTestSuite subject() {
        return functionalTest(project);
    }

    @Test
    void allFunctionalTestTasksRunsAfterAllUnitTestTasks() {
        assertThat(functionalTest(project).getTestTasks().getElements(),
                providerOf(everyItem(shouldRunAfter(containsInAnyOrder(named("test6.7"), named("test7.0"))))));
    }
}
