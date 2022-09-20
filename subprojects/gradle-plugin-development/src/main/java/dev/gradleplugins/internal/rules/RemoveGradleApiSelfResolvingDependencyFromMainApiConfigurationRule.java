package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;

@RuleGroup(ExternalGradleApiGroup.class)
public final class RemoveGradleApiSelfResolvingDependencyFromMainApiConfigurationRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        // Surgical procedure of removing the Gradle API and replacing it with dev.gradleplugins:gradle-api
        project.getConfigurations().getByName("api").getDependencies().removeIf(it -> {
            if (it instanceof SelfResolvingDependencyInternal) {
                return ((SelfResolvingDependencyInternal) it).getTargetComponentId().getDisplayName().equals("Gradle API");
            }
            return false;
        });
    }
}
