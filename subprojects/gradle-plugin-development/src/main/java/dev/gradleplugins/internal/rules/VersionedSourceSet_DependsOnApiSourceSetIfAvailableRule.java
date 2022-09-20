package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import dev.gradleplugins.internal.util.SourceSetUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

import static dev.gradleplugins.internal.rules.VersionedSourceSetUtils.apiClassifier;
import static dev.gradleplugins.internal.rules.VersionedSourceSetUtils.asDependency;
import static dev.gradleplugins.internal.rules.VersionedSourceSetUtils.isVersionedSourceSet;
import static dev.gradleplugins.internal.util.DomainObjectCollectionUtils.matching;
import static dev.gradleplugins.internal.util.DomainObjectCollectionUtils.singleOrEmpty;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.apiSourceSet;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

@RuleGroup(VersionedSourceSetGroup.class)
public final class VersionedSourceSet_DependsOnApiSourceSetIfAvailableRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        SourceSetUtils.sourceSets(project, it -> it.configureEach(matching(isVersionedSourceSet(), sourceSet -> {
            project.getConfigurations().named(sourceSet.getImplementationConfigurationName(), configuration -> {
                configuration.getDependencies().addAllLater(project.getObjects().setProperty(Dependency.class)
                        .value(singleOrEmpty(apiSourceSet(gradlePlugin(project))
                                .map(__ -> apiClassifier())
                                .map(asDependency(project)))));
            });
        })));
    }
}
