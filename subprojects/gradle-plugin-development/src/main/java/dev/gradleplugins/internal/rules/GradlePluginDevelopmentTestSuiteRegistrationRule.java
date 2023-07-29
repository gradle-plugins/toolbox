package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.component.SoftwareComponent;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;

public final class GradlePluginDevelopmentTestSuiteRegistrationRule implements Action<Project> {
    private final String testSuiteName;

    public GradlePluginDevelopmentTestSuiteRegistrationRule(String testSuiteName) {
        this.testSuiteName = testSuiteName;
    }

    @Override
    public void execute(Project project) {
        final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
        final GradlePluginDevelopmentTestSuite testSuite = factory.create(testSuiteName);
        testSuite.getSourceSet().disallowChanges();

        project.getExtensions().add(testSuiteName, testSuite);
        project.getComponents().add((SoftwareComponent) testSuite);
    }
}
