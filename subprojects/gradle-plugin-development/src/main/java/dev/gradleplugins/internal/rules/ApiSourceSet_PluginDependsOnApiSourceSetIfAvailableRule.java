package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.SourceSet;

import static dev.gradleplugins.internal.rules.VersionedSourceSetUtils.apiClassifier;
import static dev.gradleplugins.internal.rules.VersionedSourceSetUtils.asDependency;
import static dev.gradleplugins.internal.util.DomainObjectCollectionUtils.singleOrEmpty;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.apiSourceSet;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

@RuleGroup(ApiSourceSetGroup.class)
// Finalizer rule because it needs plugin source set
public class ApiSourceSet_PluginDependsOnApiSourceSetIfAvailableRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        final SourceSet sourceSet = gradlePlugin(project).getPluginSourceSet();
        project.getConfigurations().named(sourceSet.getApiConfigurationName(), configuration -> {
            configuration.getDependencies().addAllLater(project.getObjects().setProperty(Dependency.class)
                    .value(singleOrEmpty(apiSourceSet(gradlePlugin(project))
                            .map(__ -> apiClassifier())
                            .map(asDependency(project)))));
        });
    }
}
