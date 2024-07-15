package dev.gradleplugins;

import org.gradle.api.tasks.SourceSet;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.hamcrest.gradle.GradleProviderMatchers.PropertyTarget.ofOwner;
import static dev.gradleplugins.hamcrest.gradle.GradleProviderMatchers.forChangesDisallowed;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertThat(ex.getMessage(), forChangesDisallowed(ofOwner(subject()).property("sourceSet")));
    }

    @Test
    default void disallowChangesToTestingStrategiesProperty() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject().getTestingStrategies().set(singleton(mock(GradlePluginTestingStrategy.class))));
        assertThat(ex.getMessage(), forChangesDisallowed(ofOwner(subject()).property("testingStrategies")));
    }

    @Test
    default void disallowChangesToTestedSourceSetProperty() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject().getTestedSourceSet().set(mock(SourceSet.class)));
        assertThat(ex.getMessage(), forChangesDisallowed(ofOwner(subject()).property("testedSourceSet")));
    }
}
