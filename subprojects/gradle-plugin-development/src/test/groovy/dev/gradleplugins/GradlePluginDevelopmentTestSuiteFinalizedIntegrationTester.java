package dev.gradleplugins;

import org.gradle.api.tasks.SourceSet;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

interface GradlePluginDevelopmentTestSuiteFinalizedIntegrationTester {
    GradlePluginDevelopmentTestSuite subject();

    @Test
    default void canFinalizeTestSuiteMultipleTime() {
        assertDoesNotThrow(subject()::finalizeComponent);
    }

    @Test
    default void disallowChangesToTestSuiteSourceSetProperty() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject().getSourceSet().set(mock(SourceSet.class)));
        assertEquals("The value for " + subject() + " property 'sourceSet' is final and cannot be changed any further.", ex.getMessage());
    }

    @Test
    default void disallowChangesToTestingStrategiesProperty() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject().getTestingStrategies().set(singleton(mock(GradlePluginTestingStrategy.class))));
        assertEquals("The value for " + subject() + " property 'testingStrategies' is final and cannot be changed any further.", ex.getMessage());
    }

    @Test
    default void disallowChangesToTestedSourceSetProperty() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject().getTestedSourceSet().set(mock(SourceSet.class)));
        assertEquals("The value for " + subject() + " property 'testedSourceSet' cannot be changed any further.", ex.getMessage());
    }
}
