package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.*;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class GradlePluginDevelopmentBasePluginIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void appliesSubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.base");
        project.getPluginManager().apply("java-gradle-plugin");
    }

    @Test
    void registerCompatibilityExtension() {
        assertThat(gradlePlugin(project), extensions(hasItem(allOf(named("compatibility"), publicType(GradlePluginDevelopmentCompatibilityExtension.class)))));
    }

    @Test
    void removesSelfResolvingGradleApiDependency() {
        assertThat(project.getConfigurations().getByName("api").getDependencies(), not(hasItem(isA(SelfResolvingDependency.class))));
    }
}
