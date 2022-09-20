package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.capabilities;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

class VersionedSourceSet_AddVersionedComponentDependencyToPluginSourceSetAsImplementationDependencyRuleIntegrationTests {
    Project project = ProjectBuilder.builder().withName("foo").build();
    Action<Project> subject = new VersionedSourceSet_AddVersionedComponentDependencyToPluginSourceSetAsImplementationDependencyRule();

    @BeforeEach
    void configureProject() {
        project.setGroup("com.example");
        project.setVersion("5.2");
        project.getPluginManager().apply("java-gradle-plugin");
        sourceSets(project, it -> {
            it.create("v6.5");
            it.create("v6.8");
            it.create("v7.0");
            gradlePlugin(project).pluginSourceSet(it.create("plugin"));
        });
    }

    @Test
    void addsImplementationDependencyOnEachVersionedComponents() {
        subject.execute(project);
        assertThat(project.getConfigurations().getByName("pluginImplementation").getDependencies(),
                hasItems(
                        allOf(coordinate("com.example:foo:5.2"), capabilities(hasItem(coordinate("com.example:foo-gradle-v6.5:5.2")))),
                        allOf(coordinate("com.example:foo:5.2"), capabilities(hasItem(coordinate("com.example:foo-gradle-v6.8:5.2")))),
                        allOf(coordinate("com.example:foo:5.2"), capabilities(hasItem(coordinate("com.example:foo-gradle-v7.0:5.2"))))
                ));
    }
}
