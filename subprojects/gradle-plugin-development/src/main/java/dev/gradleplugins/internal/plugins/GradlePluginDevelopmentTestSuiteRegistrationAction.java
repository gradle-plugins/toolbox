package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.component.SoftwareComponent;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;

final class GradlePluginDevelopmentTestSuiteRegistrationAction implements Action<Project> {
    private final String testSuiteName;

    GradlePluginDevelopmentTestSuiteRegistrationAction(String testSuiteName) {
        this.testSuiteName = testSuiteName;
    }

    @Override
    public void execute(Project project) {
        final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
        final GradlePluginDevelopmentTestSuite testSuite = factory.create(testSuiteName);
        testSuite.getSourceSet().disallowChanges();

        project.getExtensions().add(testSuiteName, testSuite);
        project.getComponents().add((SoftwareComponent) testSuite);

        project.afterEvaluate(proj -> testSuite.finalizeComponent());
    }
}
