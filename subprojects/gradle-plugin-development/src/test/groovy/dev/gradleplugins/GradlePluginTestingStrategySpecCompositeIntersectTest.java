package dev.gradleplugins;

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategy.Spec.matches;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradlePluginTestingStrategySpecCompositeIntersectTest {
    private final GradlePluginTestingStrategy.Spec<GradlePluginTestingStrategy> subject = matches(aStrategy()::equals).and(matches(anotherStrategy("here")::equals));

    @Test
    void isNotSatisfiedByPartialMatchingStrategy() {
        assertFalse(subject.isSatisfiedBy(aStrategy()));
        assertFalse(subject.isSatisfiedBy(anotherStrategy("here")));
    }

    @Test
    void isSatisfiedByCompositeStrategyWithAllStrategiesRegardlessOfOrder() {
        assertTrue(subject.isSatisfiedBy(aCompositeStrategy(aStrategy(), anotherStrategy("here"))));
        assertTrue(subject.isSatisfiedBy(aCompositeStrategy(anotherStrategy("here"), aStrategy())));
    }
}
