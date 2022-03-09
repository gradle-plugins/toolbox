package dev.gradleplugins;

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.publicType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

public interface GradlePluginDevelopmentTestSuiteTester {
    GradlePluginDevelopmentTestSuite subject();

    @Test
    default void hasName() {
        assertThat(subject().getName(), not(blankOrNullString()));
    }

    @Test
    default void hasTestingStrategies() {
        assertNotNull(subject().getTestingStrategies());
    }

    @Test
    default void hasSourceSet() {
        assertNotNull(subject().getSourceSet());
    }

    @Test
    default void hasTestedSourceSet() {
        assertNotNull(subject().getTestedSourceSet());
    }

    @Test
    default void hasStrategiesFactory() {
        assertNotNull(subject().getStrategies());
    }

    @Test
    default void hasTestTasks() {
        assertNotNull(subject().getTestTasks());
    }

    @Test
    default void hasComponentDependencies() {
        assertNotNull(subject().getDependencies());
    }

    @Test
    default void canConfigureComponentDependencies() {
        final MockAction<GradlePluginDevelopmentTestSuiteDependencies> action = new MockAction<>();
        subject().dependencies(action);
        assertTrue(action.isActionCalled());
        assertEquals(subject().getDependencies(), action.getArgument());
    }

    @Test
    default void hasPublicType() {
        assertThat(subject(), publicType(GradlePluginDevelopmentTestSuite.class));
    }

    @Test
    default void hasDisplayName() {
        assertThat(subject().getDisplayName(), not(blankOrNullString()));
    }

    @Test
    default void hasPluginUnderTestMetadataTask() {
        assertNotNull(subject().getPluginUnderTestMetadataTask());
    }
}
