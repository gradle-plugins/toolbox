package dev.gradleplugins.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

// TODO(2.0): Make this class "private"
abstract /*final*/ class GradlePluginDevelopmentTestingBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentTestingBasePlugin() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-development");
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
        project.getPluginManager().withPlugin("java-gradle-plugin", new RegisterTestingExtensionOnGradleDevelExtensionRule(project));
        project.afterEvaluate(new FinalizeComponents());
    }
}
