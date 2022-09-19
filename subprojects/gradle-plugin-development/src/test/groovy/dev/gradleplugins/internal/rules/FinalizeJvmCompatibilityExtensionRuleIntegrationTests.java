package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class FinalizeJvmCompatibilityExtensionRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new FinalizeJvmCompatibilityExtensionRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
        final JvmCompatibilityExtension extension = ((ExtensionAware) gradlePlugin(project)).getExtensions().create("jvm", JvmCompatibilityExtension.class);
        extension.getSourceCompatibility().set(JavaVersion.VERSION_12);
        extension.getTargetCompatibility().set(JavaVersion.VERSION_1_9);
    }

    @Test
    void overwriteJavaExtensionSourceCompatibility() {
        subject.execute(project);
        assertThat(java(project).getSourceCompatibility(), equalTo(JavaVersion.VERSION_12));
    }

    @Test
    void overwriteJavaExtensionTargetCompatibility() {
        subject.execute(project);
        assertThat(java(project).getTargetCompatibility(), equalTo(JavaVersion.VERSION_1_9));
    }
}
