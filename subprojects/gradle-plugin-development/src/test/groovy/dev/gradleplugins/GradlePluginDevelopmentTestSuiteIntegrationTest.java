package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradlePluginDevelopmentTestSuiteIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = GradlePluginDevelopmentTestSuiteFactory.forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("gote");

    @Test
    void hasTestingStrategies() {
        assertNotNull(subject.getTestingStrategies());
    }

    @Test
    void hasTestedSourceSet() {
        assertNotNull(subject.getTestedSourceSet());
    }

    @Test
    void hasStrategiesFactory() {
        assertNotNull(subject.getStrategies());
    }

    @Test
    void hasTestTasks() {
        assertNotNull(subject.getTestTasks());
    }

    @Test
    void hasComponentDependencies() {
        assertNotNull(subject.getDependencies());
    }

    @Test
    void canConfigureComponentDependencies() {
        final MockAction<GradlePluginDevelopmentTestSuiteDependencies> action = new MockAction<>();
        subject.dependencies(action);
        assertTrue(action.isActionCalled());
        assertEquals(subject.getDependencies(), action.getArgument());
    }
}
