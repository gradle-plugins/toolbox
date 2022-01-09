package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;

class GradlePluginDevelopmentTestSuiteCoreGradleDevelPluginAppliedIntegrationTest implements GradlePluginDevelopmentTestSuiteCoreGradleDevelPluginAppliedIntegrationTester {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("jike");

    @Override
    public GradlePluginDevelopmentTestSuite subject() {
        return subject;
    }

    @Override
    public Project project() {
        return project;
    }
}
