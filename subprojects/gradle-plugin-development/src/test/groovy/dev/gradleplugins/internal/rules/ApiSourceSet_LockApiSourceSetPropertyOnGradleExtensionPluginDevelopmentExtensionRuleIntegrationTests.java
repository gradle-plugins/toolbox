package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.apiSourceSet;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiSourceSet_LockApiSourceSetPropertyOnGradleExtensionPluginDevelopmentExtensionRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new ApiSourceSet_LockApiSourceSetPropertyOnGradleExtensionPluginDevelopmentExtensionRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
        ((ExtensionAware) gradlePlugin(project)).getExtensions()
                .add("apiSourceSet", project.getObjects().property(SourceSet.class));
    }

    @Test
    void disallowChangesOnApiSourceSetProperty() {
        subject.execute(project);
        assertThrows(IllegalStateException.class,
                () -> apiSourceSet(gradlePlugin(project)).set((SourceSet) null));
    }
}
