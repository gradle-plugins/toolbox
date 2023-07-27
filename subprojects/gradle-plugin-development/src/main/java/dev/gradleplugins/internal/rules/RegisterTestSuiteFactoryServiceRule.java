package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import org.gradle.api.Action;
import org.gradle.api.Project;

public final class RegisterTestSuiteFactoryServiceRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        project.getExtensions().add("testSuiteFactory", GradlePluginDevelopmentTestSuiteFactory.forProject(project));
    }
}
