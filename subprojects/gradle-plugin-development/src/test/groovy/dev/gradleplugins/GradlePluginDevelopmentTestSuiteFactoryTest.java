package dev.gradleplugins;

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradlePluginDevelopmentTestSuiteFactoryTest {
    @Test
    void throwsExceptionIfProjectIsNullOnFactoryMethod() {
        assertThrows(NullPointerException.class, () -> forProject(null));
    }
}
