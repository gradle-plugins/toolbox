package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GradlePluginDevelopmentRepositoryExtensionTest {
    Project project;
    GradlePluginDevelopmentRepositoryExtension subject;

    @BeforeEach
    void givenExtendedProject() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-development");
        subject = GradlePluginDevelopmentRepositoryExtension.from(project.getRepositories());
    }

    @Test
    void returnsGradlePluginDevelopmentRepository() {
        MavenArtifactRepository repository = subject.gradlePluginDevelopment();
        assertNotNull(repository);
        assertEquals("Gradle Plugin Development", repository.getName());
    }

    @Test
    void returnsGradlePluginDevelopmentRepositoryUsingAction() {
        MavenArtifactRepository repository = subject.gradlePluginDevelopment(it -> { /* do nothing */ });
        assertNotNull(repository);
        assertEquals("Gradle Plugin Development", repository.getName());
    }

    @Test
    void callsConfigureActionAfterBaseConfiguration() {
        // Good enough approximation
        MavenArtifactRepository repository = subject.gradlePluginDevelopment(it -> it.setName("Foo Repository"));
        assertEquals("Foo Repository", repository.getName());
    }
}
