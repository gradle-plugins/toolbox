package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.rules.RegisterTestSuiteFactoryServiceRule;
import org.gradle.api.Action;
import org.gradle.api.Project;

final class FinalizeComponents implements Action<Project> {
    @Override
    public void execute(Project project) {
        project.getComponents().withType(RegisterTestSuiteFactoryServiceRule.DefaultGradlePluginDevelopmentTestSuiteFactory.GradlePluginDevelopmentTestSuiteInternal.class).configureEach(RegisterTestSuiteFactoryServiceRule.DefaultGradlePluginDevelopmentTestSuiteFactory.GradlePluginDevelopmentTestSuiteInternal::finalizeComponent);
    }
}
