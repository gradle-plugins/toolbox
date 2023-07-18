package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.extensions;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.publicType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

class GradlePluginDevelopmentPluginProjectIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void setup() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-development");
    }

    @Test
    void registersGradlePluginDevelopmentExtensionOnRepositoryHandler() {
        assertThat(project.getRepositories(), extensions(hasItem(allOf(named("$extension_GradlePluginDevelopmentRepositoryExtension"), publicType(GradlePluginDevelopmentRepositoryExtension.class)))));
    }

    @Test
    void registersGradlePluginDevelopmentExtensionOnDependencyHandler() {
        assertThat(project.getDependencies(), extensions(hasItem(allOf(named("$extension_GradlePluginDevelopmentDependencyExtension"), publicType(GradlePluginDevelopmentDependencyExtension.class)))));
    }
}
