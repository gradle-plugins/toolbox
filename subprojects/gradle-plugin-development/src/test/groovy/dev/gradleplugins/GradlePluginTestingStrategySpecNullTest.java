package dev.gradleplugins;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static dev.gradleplugins.GradlePluginTestingStrategy.Spec.matches;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradlePluginTestingStrategySpecNullTest {
    @Test
    void throwsExceptionWhenPredicateIsNull() {
        assertThrows(NullPointerException.class, () -> matches(null));
    }

    @Test
    void throwsExceptionWhenOrSpecIsNull() {
        assertThrows(NullPointerException.class, () -> matches(alwaysTrue()).or(null));
    }

    @Test
    void throwsExceptionWhenAndSpecIsNull() {
        assertThrows(NullPointerException.class, () -> matches(alwaysTrue()).and(null));
    }

    private static <T> Predicate<T> alwaysTrue() {
        return t -> true;
    }
}
