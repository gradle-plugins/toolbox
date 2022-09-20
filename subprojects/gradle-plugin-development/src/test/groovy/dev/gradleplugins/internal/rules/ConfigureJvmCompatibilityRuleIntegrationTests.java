package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.rules.JvmCompatibilityExtension.jvm;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ConfigureJvmCompatibilityRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new ConfigureJvmCompatibilityRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
        ((ExtensionAware) gradlePlugin(project)).getExtensions().create("jvm", JvmCompatibilityExtension.class);
    }

    @Nested
    class SourceCompatibilityTests {
        @Test
        void setJavaPluginToVersion1_1() {
            subject.execute(project);
            assertThat(java(project).getSourceCompatibility(), equalTo(JavaVersion.VERSION_1_1));
        }

        @Test
        void usesMinimumJavaVersionFromMinimumGradleVersionWhenAvailable() {
            subject.execute(project);
            new RegisterCompatibilityExtensionGradlePluginDevelopmentExtensionRule().execute(project);
            compatibility(gradlePlugin(project)).getMinimumGradleVersion().set("4.2");

            assertThat(jvm(gradlePlugin(project)).getSourceCompatibility(), providerOf(JavaVersion.VERSION_1_7));
        }

        @Test
        void usesOriginalValueWhenNoMinimumGradleVersionAvailable() {
            final JavaVersion originalValue = java(project).getSourceCompatibility();
            subject.execute(project);

            assertThat(jvm(gradlePlugin(project)).getSourceCompatibility(), providerOf(originalValue));
        }

        @Test
        void usesJavaPluginExtensionValueWhenSet() {
            subject.execute(project);
            java(project).setSourceCompatibility(JavaVersion.VERSION_12);

            assertThat(jvm(gradlePlugin(project)).getSourceCompatibility(), providerOf(JavaVersion.VERSION_12));
        }
    }

    @Nested
    class TargetCompatibilityTests {
        @Test
        void setJavaPluginToVersion1_1() {
            subject.execute(project);
            assertThat(java(project).getTargetCompatibility(), equalTo(JavaVersion.VERSION_1_1));
        }

        @Test
        void usesMinimumJavaVersionFromMinimumGradleVersionWhenAvailable() {
            subject.execute(project);
            new RegisterCompatibilityExtensionGradlePluginDevelopmentExtensionRule().execute(project);
            compatibility(gradlePlugin(project)).getMinimumGradleVersion().set("4.2");

            assertThat(jvm(gradlePlugin(project)).getTargetCompatibility(), providerOf(JavaVersion.VERSION_1_7));
        }

        @Test
        void usesOriginalValueWhenNoMinimumGradleVersionAvailable() {
            final JavaVersion originalValue = java(project).getTargetCompatibility();
            subject.execute(project);

            assertThat(jvm(gradlePlugin(project)).getTargetCompatibility(), providerOf(originalValue));
        }

        @Test
        void usesJavaPluginExtensionValueWhenSet() {
            subject.execute(project);
            java(project).setTargetCompatibility(JavaVersion.VERSION_12);

            assertThat(jvm(gradlePlugin(project)).getTargetCompatibility(), providerOf(JavaVersion.VERSION_12));
        }
    }
}
