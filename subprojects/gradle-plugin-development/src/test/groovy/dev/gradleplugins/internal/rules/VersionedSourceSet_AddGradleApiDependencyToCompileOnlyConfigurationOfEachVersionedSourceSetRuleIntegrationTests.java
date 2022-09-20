package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class VersionedSourceSet_AddGradleApiDependencyToCompileOnlyConfigurationOfEachVersionedSourceSetRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new VersionedSourceSet_AddGradleApiDependencyToCompileOnlyConfigurationOfEachVersionedSourceSetRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java");
    }

    @Test
    void addsGradleApiDependencyToCompileOnly() {
        subject.execute(project);
        sourceSets(project, it -> {
            it.create("v3.4");
            it.create("v6.9");
            it.create("v7.2.1");
        });

        assertThat(project.getConfigurations().getByName("v34CompileOnly").getDependencies(),
                hasItem(coordinate("dev.gradleplugins:gradle-api:3.4")));
        assertThat(project.getConfigurations().getByName("v69CompileOnly").getDependencies(),
                hasItem(coordinate("dev.gradleplugins:gradle-api:6.9")));
        assertThat(project.getConfigurations().getByName("v721CompileOnly").getDependencies(),
                hasItem(coordinate("dev.gradleplugins:gradle-api:7.2.1")));
    }
}
