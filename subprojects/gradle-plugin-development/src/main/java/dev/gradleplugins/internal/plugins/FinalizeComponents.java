package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import org.gradle.api.Action;
import org.gradle.api.Project;

final class FinalizeComponents implements Action<Project> {
    @Override
    public void execute(Project project) {
        project.getComponents().withType(GradlePluginDevelopmentTestSuiteInternal.class).configureEach(GradlePluginDevelopmentTestSuiteInternal::finalizeComponent);
    }
}
