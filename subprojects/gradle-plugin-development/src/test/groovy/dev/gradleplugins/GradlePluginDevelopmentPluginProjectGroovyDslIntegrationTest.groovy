package dev.gradleplugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static dev.gradleplugins.ProjectMatchers.named
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasItem

class GradlePluginDevelopmentPluginProjectGroovyDslIntegrationTest {
    private final Project project = ProjectBuilder.builder().build()

    @BeforeEach
    void setup() {
        project.pluginManager.apply('dev.gradleplugins.gradle-plugin-development')
    }

    @Test
    void canAddRepositoryViaGradlePluginDevelopmentMethodCallOnRepositoryHandler() {
        project.repositories.gradlePluginDevelopment()
        assertThat(project.repositories, hasItem(named('Gradle Plugin Development')))
    }

    @Test
    void canAddRepositoryViaGradlePluginDevelopmentOnRepositoryDsl() {
        project.repositories {
            gradlePluginDevelopment()
        }
        assertThat(project.repositories, hasItem(named('Gradle Plugin Development')))
    }
}
