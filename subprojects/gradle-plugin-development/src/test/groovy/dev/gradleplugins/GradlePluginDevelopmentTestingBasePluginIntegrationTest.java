package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

class GradlePluginDevelopmentTestingBasePluginIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void applyPlugins() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
    }

    @Test
    void appliesBasePlugin() {
        assertThat(project, hasPlugin("dev.gradleplugins.gradle-plugin-base"));
    }

    @Test
    void appliesDevelPlugin() {
        assertThat(project, hasPlugin("dev.gradleplugins.gradle-plugin-development"));
    }

    @Test
    void registersTestingExtensionOnGradleDevelExtension() {
        project.getPluginManager().apply("java-gradle-plugin");
        assertThat(project.getExtensions().getByType(GradlePluginDevelopmentExtension.class),
                extensions(hasItem(allOf(named("testing"), publicType(GradlePluginDevelopmentTestingExtension.class)))));
    }
}
