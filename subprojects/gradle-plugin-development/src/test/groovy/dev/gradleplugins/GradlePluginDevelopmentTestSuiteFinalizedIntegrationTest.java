package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GradlePluginDevelopmentTestSuiteFinalizedIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("leek");

    @BeforeEach
    void finalizeTestSuite() {
        subject.getSourceSet().set(mock(SourceSet.class));
        subject.finalizeComponent();
    }

    @Test
    void canFinalizeTestSuiteMultipleTime() {
        assertDoesNotThrow(subject::finalizeComponent);
    }

    @Test
    void disallowChangesToTestSuiteSourceSetProperty() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject.getSourceSet().set(mock(SourceSet.class)));
        assertEquals("The value for test suite 'leek' property 'sourceSet' is final and cannot be changed any further.", ex.getMessage());
    }

    @Test
    void disallowChangesToTestingStrategiesProperty() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject.getTestingStrategies().set(singleton(mock(GradlePluginTestingStrategy.class))));
        assertEquals("The value for test suite 'leek' property 'testingStrategies' is final and cannot be changed any further.", ex.getMessage());
    }

    @Test
    void disallowChangesToTestedSourceSetProperty() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject.getTestedSourceSet().set(mock(SourceSet.class)));
        assertEquals("The value for test suite 'leek' property 'testedSourceSet' cannot be changed any further.", ex.getMessage());
    }
}
