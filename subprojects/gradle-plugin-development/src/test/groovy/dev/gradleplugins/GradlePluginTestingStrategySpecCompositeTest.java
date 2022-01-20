package dev.gradleplugins;

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategy.Spec.matches;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradlePluginTestingStrategySpecCompositeTest {
    private final GradlePluginTestingStrategy.Spec<GradlePluginTestingStrategy> subject = matches(aCompositeStrategy(aStrategy(), anotherStrategy("kloe"))::equals);

    @Test
    void isSatisfiedByExactMatchingCompositeStrategy() {
        assertTrue(subject.isSatisfiedBy(aCompositeStrategy(aStrategy(), anotherStrategy("kloe"))));
    }

    @Test
    void isNotSatisfiedByIndividualStrategyOfComposition() {
        assertFalse(subject.isSatisfiedBy(aStrategy()));
        assertFalse(subject.isSatisfiedBy(anotherStrategy("kloe")));
    }
}
