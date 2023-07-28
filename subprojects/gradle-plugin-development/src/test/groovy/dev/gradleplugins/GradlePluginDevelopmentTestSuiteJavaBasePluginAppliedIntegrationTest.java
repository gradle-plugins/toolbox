package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

class GradlePluginDevelopmentTestSuiteJavaBasePluginAppliedIntegrationTest {
    Project project = ProjectBuilder.builder().build();
    GradlePluginDevelopmentTestSuiteFactory factory;
    GradlePluginDevelopmentTestSuite subject;

    @BeforeEach
    void applyJavaBasePlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        project.getPluginManager().apply("java-base");
        factory = forProject(project);
        subject = factory.create("loke");
    }

    @Test
    void createsDefaultSourceSetOnFinalize() {
        subject.finalizeComponent();
        assertThat(sourceSets(project), hasItem(named("loke")));
    }

    @Test
    void createsDefaultSourceSetOnSourceSetPropertyQueryOfConvention() {
        subject.getSourceSet().set((SourceSet) null);
        assertThat(subject.getSourceSet().get(), named("loke"));
        assertThat(sourceSets(project), hasItem(named("loke")));
    }

    @Test
    void doesNotCreateDefaultSourceSetOnFinalizeWhenSourceSetPropertyOverridden() {
        subject.getSourceSet().set(sourceSets(project).create("kiel"));
        subject.finalizeComponent();
        assertThat(sourceSets(project), not(hasItem(named("loke"))));
    }

    @Test
    void doesNotCreateDefaultSourceSetOnSourceSetPropertyQueryWhenSourceSetPropertyOverridden() {
        subject.getSourceSet().set(sourceSets(project).create("lope"));
        assertThat(subject.getSourceSet().get(), named("lope"));
        assertThat(sourceSets(project), not(hasItem(named("loke"))));
    }

    @Test
    void doesNotCreateDefaultSourceSetOnTestSuiteCreation() {
        assertThat(sourceSets(project), not(hasItem(named("loke"))));
    }
}
