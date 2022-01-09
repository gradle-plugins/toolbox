package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

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
    void createsDefaultSourceSetOnSourceSetPropertyQuery() {
        assertThat(subject.getSourceSet().get(), named("loke"));
        assertThat(project.getExtensions().getByType(SourceSetContainer.class), hasItem(named("loke")));
    }
}
