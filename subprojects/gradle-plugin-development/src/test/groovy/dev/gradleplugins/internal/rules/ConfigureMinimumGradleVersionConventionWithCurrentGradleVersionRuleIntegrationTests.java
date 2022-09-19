package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.util.GradleTestUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;

class ConfigureMinimumGradleVersionConventionWithCurrentGradleVersionRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new ConfigureMinimumGradleVersionConventionWithCurrentGradleVersionRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
        ((ExtensionAware) gradlePlugin(project)).getExtensions()
                .create("compatibility", GradlePluginDevelopmentCompatibilityExtension.class);
    }

    @Test
    void defaultsMinimumGradleVersionConventionToCurrentGradleVersion() {
        GradleTestUtils.setCurrentGradleVersion(GradleVersion.version("6.7"));
        subject.execute(project);
        compatibility(gradlePlugin(project)).getMinimumGradleVersion().set((String) null);

        assertThat(compatibility(gradlePlugin(project)).getMinimumGradleVersion().value((String) null), providerOf("6.7"));
    }
}
