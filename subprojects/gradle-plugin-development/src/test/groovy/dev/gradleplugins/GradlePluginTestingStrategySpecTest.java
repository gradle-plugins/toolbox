package dev.gradleplugins;

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategy.Spec.matches;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradlePluginTestingStrategySpecTest {
    private final GradlePluginTestingStrategy.Spec<GradlePluginTestingStrategy> subject = matches(aStrategy()::equals);

    @Test
    void isSatisfiedByExactMatchingStrategy() {
        assertTrue(subject.isSatisfiedBy(aStrategy()));
    }

    @Test
    void isNotSatisfiedByAnotherStrategyThanMatchingStrategy() {
        assertFalse(subject.isSatisfiedBy(anotherStrategy()));
    }

    @Test
    void isSatisfiedByCompositeStrategyContainingMatchingStrategy() {
        assertTrue(subject.isSatisfiedBy(aCompositeStrategy(anotherStrategy(), aStrategy())));
    }

    @Test
    void isNotSatisfiedByCompositeStrategyNotContainingMatchingStrategy() {
        assertFalse(subject.isSatisfiedBy(aCompositeStrategy(anotherStrategy(), anotherStrategy("dkel"))));
    }
}
