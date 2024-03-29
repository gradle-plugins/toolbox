package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.hasPlugin;
import static org.hamcrest.MatcherAssert.assertThat;

class JavaGradlePluginDevelopmentPluginIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void applySubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.java-gradle-plugin");
    }

    @Test
    void appliesGradlePluginBasePlugin() {
        assertThat(project, hasPlugin("dev.gradleplugins.gradle-plugin-base"));
    }

    @Test
    void appliesGradlePluginTestingBasePlugin() {
        assertThat(project, hasPlugin("dev.gradleplugins.gradle-plugin-testing-base"));
    }
}
