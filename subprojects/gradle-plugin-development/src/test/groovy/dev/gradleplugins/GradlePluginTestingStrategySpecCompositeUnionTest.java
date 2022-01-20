package dev.gradleplugins;

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategy.Spec.matches;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradlePluginTestingStrategySpecCompositeUnionTest {
    private final GradlePluginTestingStrategy.Spec<GradlePluginTestingStrategy> subject = matches(aStrategy()::equals).or(matches(anotherStrategy("desy")::equals));

    @Test
    void isSatisfiedByAnyIndividualStrategyOfComposition() {
        assertTrue(subject.isSatisfiedBy(aStrategy()));
        assertTrue(subject.isSatisfiedBy(anotherStrategy("desy")));
    }

    @Test
    void isSatisfiedByCompositionOfAnyIndividualStrategy() {
        assertTrue(subject.isSatisfiedBy(aCompositeStrategy(aStrategy(), anotherStrategy("pole"))));
        assertTrue(subject.isSatisfiedBy(aCompositeStrategy(anotherStrategy("desy"), anotherStrategy("pole"))));
    }

    @Test
    void isSatisfiedByCompositionOfAllIndividualStrategy() {
        assertTrue(subject.isSatisfiedBy(aCompositeStrategy(aStrategy(), anotherStrategy("desy"))));
        assertTrue(subject.isSatisfiedBy(aCompositeStrategy(anotherStrategy("desy"), aStrategy())));
    }

    @Test
    void isNotSatisfiedByUnrelatedStrategy() {
        assertFalse(subject.isSatisfiedBy(aCompositeStrategy(anotherStrategy("lodk"), anotherStrategy("ertd"))));
        assertFalse(subject.isSatisfiedBy(anotherStrategy("lope")));
    }
}
