package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;
import static org.mockito.Mockito.mock;

class GradlePluginDevelopmentTestSuiteFinalizeComponentIntegrationTest implements GradlePluginDevelopmentTestSuiteFinalizedIntegrationTester {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("leek");

    @Override
    public GradlePluginDevelopmentTestSuite subject() {
        return subject;
    }

    @BeforeEach
    void finalizeTestSuite() {
        project.getPluginManager().apply("java-base");
        subject.getSourceSet().set(sourceSets(project).create("leek"));
        subject.finalizeComponent();
    }
}
