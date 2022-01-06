package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GradlePluginDevelopmentCompatibilityExtensionIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private GradlePluginDevelopmentCompatibilityExtension subject;

    @BeforeEach
    void setup() {
        project.getPluginManager().apply("dev.gradleplugins.base");
        project.getPluginManager().apply("java-gradle-plugin");
        subject = compatibility(gradlePlugin(project));
    }

    @Test
    void hasMinimumGradleVersion() {
        assertNotNull(subject.getMinimumGradleVersion());
    }

    @Test
    void hasGradleApiVersion() {
        assertNotNull(subject.getGradleApiVersion());
    }
}
