package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static org.gradle.api.JavaVersion.*;
import static org.junit.jupiter.api.Assertions.*;

class JvmSourceCompatibilityPropertyIntegrationTest {
    private static final JavaVersion INITIAL_VALUE = VERSION_1_8;
    private final Project project = ProjectBuilder.builder().build();
    private JvmSourceCompatibilityProperty subject;

    @BeforeEach
    void setup() {
        project.getPluginManager().apply("java-base");
        java(project).setSourceCompatibility(INITIAL_VALUE);
        assertNotEquals(VERSION_1_1, INITIAL_VALUE);
        subject = JvmCompatibilityPropertyFactory.ofSourceCompatibility(java(project));
    }

    @Test
    void throwsNullPointerExceptionWhenSetValueIsNull() {
        assertThrows(NullPointerException.class, () -> subject.set(null));
    }

    @Test
    void initializesToJavaVersion1_1() {
        assertEquals(VERSION_1_1, java(project).getSourceCompatibility());
    }

    @Test
    void restoresInitialValueOnFinalizeValue() {
        subject.finalizeValue();
        assertEquals(INITIAL_VALUE, java(project).getSourceCompatibility());
    }

    @Test
    void usesNewValueOnSet() {
        subject.set(VERSION_1_9);
        assertEquals(VERSION_1_9, java(project).getSourceCompatibility());
    }

    @Nested
    class WhenExtensionValueChangedTest {
        @BeforeEach
        void changesExtensionValue() {
            java(project).setSourceCompatibility(VERSION_11);
        }

        @Test
        void returnsCurrentExtensionValue() {
            assertEquals(VERSION_11, subject.get());
        }

        @Test
        void doesNotRestoreInitialValueOnFinalizeValue() {
            subject.finalizeValue();
            assertNotEquals(INITIAL_VALUE, VERSION_11);
            assertEquals(VERSION_11, java(project).getSourceCompatibility());
        }

        @Test
        void ignoresNewValueOnSet() {
            subject.set(VERSION_1_9);
            assertEquals(VERSION_11, java(project).getSourceCompatibility());
        }
    }
}
