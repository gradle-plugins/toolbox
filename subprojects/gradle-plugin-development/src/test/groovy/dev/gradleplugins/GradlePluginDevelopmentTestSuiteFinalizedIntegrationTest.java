package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertThrows(RuntimeException.class, () -> subject.getSourceSet().set(mock(SourceSet.class)));
    }

    @Test
    void disallowChangesToTestingStrategiesProperty() {
        assertThrows(RuntimeException.class, () -> subject.getTestingStrategies().set(singleton(mock(GradlePluginTestingStrategy.class))));
    }

    @Test
    void disallowChangesToTestedSourceSetProperty() {
        assertThrows(RuntimeException.class, () -> subject.getTestedSourceSet().set(mock(SourceSet.class)));
    }
}
