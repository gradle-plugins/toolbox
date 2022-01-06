package dev.gradleplugins;

import org.gradle.api.JavaVersion;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradleRuntimeCompatibility.*;
import static org.junit.jupiter.api.Assertions.*;

class GradleRuntimeCompatibilityTest {
    @Test
    void canQueryMinimumGroovyVersionForSpecificGradleVersion() {
        assertEquals("2.5.8", groovyVersionOf("6.2.1"));
        assertEquals("2.4.4", groovyVersionOf("2.14"));
    }

    @Test
    void canQueryMinimumJavaVersionForSpecificGradleVersion() {
        assertEquals(JavaVersion.VERSION_1_8, minimumJavaVersionFor("6.2.1"));
        assertEquals(JavaVersion.VERSION_1_6, minimumJavaVersionFor("2.14"));
    }

    @Test
    void canQueryMinimumKotlinVersionForSpecificGradleVersion() {
        assertTrue(kotlinVersionOf("6.2.1").isPresent());
        assertEquals("1.3.61", kotlinVersionOf("6.2.1").get());
        assertFalse(kotlinVersionOf("2.14").isPresent());
    }
}
