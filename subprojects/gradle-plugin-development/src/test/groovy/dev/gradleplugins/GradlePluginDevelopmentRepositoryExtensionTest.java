package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GradlePluginDevelopmentRepositoryExtensionTest {
    private final Project project = ProjectBuilder.builder().build();
    private GradlePluginDevelopmentRepositoryExtension subject;

    @BeforeEach
    void setup() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
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
