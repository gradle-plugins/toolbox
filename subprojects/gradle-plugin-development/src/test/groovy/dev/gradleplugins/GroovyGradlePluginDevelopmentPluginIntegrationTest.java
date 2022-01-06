package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.hasPlugin;
import static org.hamcrest.MatcherAssert.assertThat;

class GroovyGradlePluginDevelopmentPluginIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void applySubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.groovy-gradle-plugin");
    }

    @Test
    void appliesBasePlugin() {
        assertThat(project, hasPlugin("dev.gradleplugins.base"));
    }
}
