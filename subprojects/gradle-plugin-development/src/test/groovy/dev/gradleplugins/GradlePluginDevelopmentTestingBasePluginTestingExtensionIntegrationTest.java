package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestingExtension.testing;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GradlePluginDevelopmentTestingBasePluginTestingExtensionIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private GradlePluginDevelopmentExtension subject;

    @BeforeEach
    void setup() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        project.getPluginManager().apply("java-gradle-plugin");
        subject = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
    }

    @Test
    void canRetrieveTestingExtensionUsingUtilityMethod() {
        assertNotNull(testing(subject));
    }
}
