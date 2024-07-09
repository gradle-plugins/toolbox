package dev.gradleplugins.internal.rules;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

/*private*/ abstract /*final*/ class ProjectExtensionsRule implements Plugin<Project> {
    @Inject
    public ProjectExtensionsRule() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("gradlepluginsdev.rules.project-repositories-extension");
        project.getPluginManager().apply("gradlepluginsdev.rules.project-dependencies-extension");
    }
}
