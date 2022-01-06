package dev.gradleplugins.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;
import org.gradle.api.plugins.AppliedPlugin;

final class RemoveGradleApiProjectDependency implements Action<AppliedPlugin> {
    private final Project project;

    RemoveGradleApiProjectDependency(Project project) {
        this.project = project;
    }

    @Override
    public void execute(AppliedPlugin ignored) {
        // Surgical procedure of removing the Gradle API and replacing it with dev.gradleplugins:gradle-api
        project.getConfigurations().getByName("api").getDependencies().removeIf(it -> {
            if (it instanceof SelfResolvingDependencyInternal) {
                return ((SelfResolvingDependencyInternal) it).getTargetComponentId().getDisplayName().equals("Gradle API");
            }
            return false;
        });
    }
}
