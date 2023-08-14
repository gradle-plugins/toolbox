package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;

class GradlePluginDevelopmentBasePluginIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void appliesSubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
        project.getPluginManager().apply("java-gradle-plugin");
        compatibility(gradlePlugin(project)).getMinimumGradleVersion().set("6.5");
    }

    @Test
    @Disabled
    void removesAllTestSourceSetsToAvoidSelfResolvingGradleTestKitDependency() {
        assertThat(gradlePlugin(project).getTestSourceSets(), emptyIterable());
    }
}
