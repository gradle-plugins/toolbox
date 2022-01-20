package dev.gradleplugins;

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategy.Spec.matches;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradlePluginTestingStrategySpecNegationTest {
    private final GradlePluginTestingStrategy.Spec<GradlePluginTestingStrategy> subject = matches(aStrategy()::equals).negate();

    @Test
    void isNotSatisfiedByExactMatchingStrategy() {
        assertFalse(subject.isSatisfiedBy(aStrategy()));
    }

    @Test
    void isNotSatisfiedByCompositionOfExactMatchingStrategy() {
        assertFalse(subject.isSatisfiedBy(aCompositeStrategy(aStrategy(), anotherStrategy("kels"))));
    }

    @Test
    void isSatisfiedByAnyOtherStrategy() {
        assertTrue(subject.isSatisfiedBy(anotherStrategy()));
        assertTrue(subject.isSatisfiedBy(anotherStrategy("dela")));
        assertTrue(subject.isSatisfiedBy(aCompositeStrategy(anotherStrategy("eodf"), anotherStrategy("eloe"))));
    }
}
