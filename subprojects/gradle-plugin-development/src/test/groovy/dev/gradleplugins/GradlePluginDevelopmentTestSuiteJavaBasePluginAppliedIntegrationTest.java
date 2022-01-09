package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.named;
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
        assertThat(project.getExtensions().getByType(SourceSetContainer.class), hasItem(named("loke")));
    }

    @Test
    void createsDefaultSourceSetOnSourceSetPropertyQueryOfConvention() {
        subject.getSourceSet().set((SourceSet) null);
        assertThat(subject.getSourceSet().get(), named("loke"));
        assertThat(project.getExtensions().getByType(SourceSetContainer.class), hasItem(named("loke")));
    }

    @Test
    void doesNotCreateDefaultSourceSetOnFinalizeWhenSourceSetPropertyOverridden() {
        subject.getSourceSet().set(project.getExtensions().getByType(SourceSetContainer.class).create("kiel"));
        subject.finalizeComponent();
        assertThat(project.getExtensions().getByType(SourceSetContainer.class), not(hasItem(named("loke"))));
    }

    @Test
    void doesNotCreateDefaultSourceSetOnSourceSetPropertyQueryWhenSourceSetPropertyOverridden() {
        subject.getSourceSet().set(project.getExtensions().getByType(SourceSetContainer.class).create("lope"));
        assertThat(subject.getSourceSet().get(), named("lope"));
        assertThat(project.getExtensions().getByType(SourceSetContainer.class), not(hasItem(named("loke"))));
    }
}
