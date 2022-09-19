package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;

class ConfigureGradleApiVersionConventionBasedOnMinimumGradleVersionRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new ConfigureGradleApiVersionConventionBasedOnMinimumGradleVersionRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
        ((ExtensionAware) gradlePlugin(project)).getExtensions()
                .create("compatibility", GradlePluginDevelopmentCompatibilityExtension.class);
    }

    @Test
    void defaultsGradleApiVersionConventionToCurrentGradleVersion() {
        subject.execute(project);
        assertThat(compatibility(gradlePlugin(project)).getGradleApiVersion().value((String) null), providerOf("local"));
    }

    @Test
    void defaultsGradleApiVersionConventionToMinimumGradleVersionWhenNonSnapshotVersion() {
        subject.execute(project);
        compatibility(gradlePlugin(project)).getMinimumGradleVersion().set("6.7");
        assertThat(compatibility(gradlePlugin(project)).getGradleApiVersion().value((String) null), providerOf("6.7"));
    }

    @Test
    void defaultsGradleApiVersionConventionToLocalVersionWhenMinimumGradleVersionIsSnapshotVersion() {
        subject.execute(project);
        compatibility(gradlePlugin(project)).getMinimumGradleVersion().set("7.6-20220916231956+0000");
        assertThat(compatibility(gradlePlugin(project)).getGradleApiVersion().value((String) null), providerOf("local"));
    }
}
