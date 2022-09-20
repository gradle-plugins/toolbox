package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;

class ApiSourceSet_ClearPluginOutgoingApiElementsWhenApiSourceSetAvailableRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new ApiSourceSet_ClearPluginOutgoingApiElementsWhenApiSourceSetAvailableRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
        gradlePlugin(project).pluginSourceSet(sourceSets(project).map(it -> it.maybeCreate("plugin")).get());
        java(project).registerFeature("plugin", spec -> spec.usingSourceSet(gradlePlugin(project).getPluginSourceSet()));
    }

    @Test
    void removesAllArtifactsOnApiElements() {
        subject.execute(project);
        final Configuration apiElements = project.getConfigurations().getByName("pluginApiElements");
        assertThat(apiElements.getOutgoing().getArtifacts(), emptyIterable());
        assertThat(apiElements.getOutgoing().getVariants(), emptyIterable());
    }
}
