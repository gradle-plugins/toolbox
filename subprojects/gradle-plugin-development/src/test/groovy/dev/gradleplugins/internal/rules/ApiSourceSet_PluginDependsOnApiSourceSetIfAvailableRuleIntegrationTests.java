package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.capabilities;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.apiSourceSet;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

class ApiSourceSet_PluginDependsOnApiSourceSetIfAvailableRuleIntegrationTests {
    Project project = ProjectBuilder.builder().withName("foo").build();
    Action<Project> subject = new ApiSourceSet_PluginDependsOnApiSourceSetIfAvailableRule();

    @BeforeEach
    void configureProject() {
        project.setGroup("com.example");
        project.setVersion("1.2");
        project.getPluginManager().apply("java-gradle-plugin");
        ((ExtensionAware) gradlePlugin(project)).getExtensions()
                .add("apiSourceSet", project.getObjects().property(SourceSet.class));
        gradlePlugin(project).pluginSourceSet(sourceSets(project).map(it -> it.maybeCreate("plugin")).get());
        java(project).registerFeature("plugin", spec -> spec.usingSourceSet(gradlePlugin(project).getPluginSourceSet()));
    }

    @Test
    void addsDependencyToApiArtifact() {
        apiSourceSet(gradlePlugin(project)).set(sourceSets(project).map(it -> it.maybeCreate("myApi")));
        subject.execute(project);
        assertThat(project.getConfigurations().getByName("pluginApi").getDependencies(),
                hasItem(allOf(coordinate("com.example:foo:1.2"), capabilities(hasItem(coordinate("com.example:foo-api:1.2"))))));
    }

    @Test
    void doesNotAddDependencyToApiArtifactWhenNotPresent() {
        subject.execute(project);
        assertThat(project.getConfigurations().getByName("pluginApi").getDependencies(),
                not(hasItem(allOf(coordinate("com.example:foo:1.2"), capabilities(hasItem(coordinate("com.example:foo-api:1.2")))))));
    }
}
