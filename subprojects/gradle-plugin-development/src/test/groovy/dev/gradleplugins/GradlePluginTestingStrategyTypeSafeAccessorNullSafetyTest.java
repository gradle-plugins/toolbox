package dev.gradleplugins;

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategy.testingStrategy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradlePluginTestingStrategyTypeSafeAccessorNullSafetyTest {
    @Test
    void throwsNullPointerExceptionWhenTaskIsNull() {
        assertThrows(NullPointerException.class, () -> testingStrategy(null));
    }
}
