package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.GroovyGradlePluginSpockTestSuite;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GradlePluginDevelopmentFunctionalTestingPlugin implements Plugin<Project> {
    private static final String FUNCTIONAL_TEST_NAME = "functionalTest";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("groovy-base");
        project.getPluginManager().apply(SpockFunctionalTestingPlugin.class);

        project.getComponents().add(project.getObjects().newInstance(GroovyGradlePluginSpockTestSuite.class, FUNCTIONAL_TEST_NAME));
    }
}
