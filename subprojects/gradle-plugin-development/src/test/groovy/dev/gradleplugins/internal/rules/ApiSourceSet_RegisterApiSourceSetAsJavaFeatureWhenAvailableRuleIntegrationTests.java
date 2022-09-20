package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.apiSourceSet;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;

class ApiSourceSet_RegisterApiSourceSetAsJavaFeatureWhenAvailableRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new ApiSourceSet_RegisterApiSourceSetAsJavaFeatureWhenAvailableRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
        ((ExtensionAware) gradlePlugin(project)).getExtensions()
                .add("apiSourceSet", project.getObjects().property(SourceSet.class));
    }

    @Test
    void registerApiFeatureWhenApiSourceSetAvailable() {
        apiSourceSet(gradlePlugin(project)).set(sourceSets(project).map(it -> it.maybeCreate("myApi")));
        subject.execute(project);

        // These configurations only exists if a feature exists for the source set
        assertThat(project.getConfigurations(), hasItems(named("myApiApi"), named("myApiApiElements"), named("myApiRuntimeElements")));
    }

    @Test
    void doesNotRegisterApiFeatureWhenApiSourceSetAvailable() {
        subject.execute(project);

        // Only the default outgoing variants exists
        assertThat(project.getConfigurations().matching(it -> it.getName().endsWith("Elements")),
                containsInAnyOrder(named("apiElements"), named("runtimeElements")));
    }
}
