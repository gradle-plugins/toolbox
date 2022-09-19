package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

class GradlePluginDevelopmentTestSuiteJavaBasePluginAppliedIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("loke");

    @BeforeEach
    void applyJavaBasePlugin() {
        project.getPluginManager().apply("java-base");
    }

    @Test
    void createsDefaultSourceSetOnFinalize() {
        subject.finalizeComponent();
        assertThat(sourceSets(project), providerOf(hasItem(named("loke"))));
    }

    @Test
    void createsDefaultSourceSetOnSourceSetPropertyQueryOfConvention() {
        subject.getSourceSet().set((SourceSet) null);
        assertThat(subject.getSourceSet().get(), named("loke"));
        assertThat(sourceSets(project), providerOf(hasItem(named("loke"))));
    }

    @Test
    void doesNotCreateDefaultSourceSetOnFinalizeWhenSourceSetPropertyOverridden() {
        subject.getSourceSet().set(sourceSets(project).map(it -> it.maybeCreate("kiel")));
        subject.finalizeComponent();
        assertThat(sourceSets(project), providerOf(not(hasItem(named("loke")))));
    }

    @Test
    void doesNotCreateDefaultSourceSetOnSourceSetPropertyQueryWhenSourceSetPropertyOverridden() {
        subject.getSourceSet().set(sourceSets(project).map(it -> it.maybeCreate("lope")));
        assertThat(subject.getSourceSet().get(), named("lope"));
        assertThat(sourceSets(project), providerOf(not(hasItem(named("loke")))));
    }

    @Test
    void doesNotCreateDefaultSourceSetOnTestSuiteCreation() {
        assertThat(sourceSets(project), providerOf(not(hasItem(named("loke")))));
    }
}
