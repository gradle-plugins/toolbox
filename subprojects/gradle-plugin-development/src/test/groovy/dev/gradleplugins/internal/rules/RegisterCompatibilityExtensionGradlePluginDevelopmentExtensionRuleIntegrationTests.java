package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.absentProvider;
import static dev.gradleplugins.ProjectMatchers.extensions;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.publicType;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

class RegisterCompatibilityExtensionGradlePluginDevelopmentExtensionRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new RegisterCompatibilityExtensionGradlePluginDevelopmentExtensionRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
    }

    @Test
    void hasCompatibilityExtension() {
        subject.execute(project);
        assertThat(gradlePlugin(project), extensions(hasItem(allOf(
                named("compatibility"),
                publicType(GradlePluginDevelopmentCompatibilityExtension.class)))));
    }

    @Test
    void hasNoMinimumGradleVersionValue() {
        subject.execute(project);
        assertThat(compatibility(gradlePlugin(project)).getMinimumGradleVersion(), absentProvider());
    }

    @Test
    void hasNoGradleApiVersionValue() {
        subject.execute(project);
        assertThat(compatibility(gradlePlugin(project)).getGradleApiVersion(), absentProvider());
    }
}
