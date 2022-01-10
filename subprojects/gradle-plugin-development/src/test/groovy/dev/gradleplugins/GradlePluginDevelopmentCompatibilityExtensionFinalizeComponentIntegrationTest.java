package dev.gradleplugins;

import dev.gradleplugins.internal.FinalizableComponent;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static org.gradle.api.JavaVersion.VERSION_11;
import static org.gradle.api.JavaVersion.VERSION_12;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GradlePluginDevelopmentCompatibilityExtensionFinalizeComponentIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private GradlePluginDevelopmentCompatibilityExtension subject;

    @BeforeEach
    void setup() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
        project.getPluginManager().apply("java-gradle-plugin");
        subject = compatibility(gradlePlugin(project));
        ((FinalizableComponent) subject).finalizeComponent();
    }

    @Test
    void canFinalizeTestSuiteMultipleTime() {
        assertDoesNotThrow(((FinalizableComponent) subject)::finalizeComponent);
    }

    @Test
    void defaultsMinimumGradleVersionToCurrentGradleVersion() {
        assertThat(subject.getMinimumGradleVersion(), providerOf(GradleVersion.current().getVersion()));
    }

    @Test
    void disallowChangesToMinimumGradleVersion() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject.getMinimumGradleVersion().set("6.9"));
        assertEquals("The value for property 'minimumGradleVersion' cannot be changed any further.", ex.getMessage());
    }

    @Test
    void disallowChangesToGradleApiVersion() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject.getGradleApiVersion().set("6.9"));
        assertEquals("The value for property 'gradleApiVersion' cannot be changed any further.", ex.getMessage());
    }

    @Test
    void doesNotChangesJvmCompatibilitiesOnAdditionalFinalize() {
        java(project).setSourceCompatibility(VERSION_11);
        java(project).setTargetCompatibility(VERSION_12);
        ((FinalizableComponent) subject).finalizeComponent();
        assertEquals(VERSION_11, java(project).getSourceCompatibility());
        assertEquals(VERSION_12, java(project).getTargetCompatibility());
    }
}
