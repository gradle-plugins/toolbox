package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public abstract class GradlePluginDevelopmentFunctionalTestingPlugin implements Plugin<Project> {
    private static final String FUNCTIONAL_TEST_NAME = "functionalTest";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentTestingBasePlugin.class);

        project.getPluginManager().withPlugin("dev.gradleplugins.java-gradle-plugin", appliedPlugin -> createFunctionalTestSuite(project));
        project.getPluginManager().withPlugin("dev.gradleplugins.groovy-gradle-plugin", appliedPlugin -> createFunctionalTestSuite(project));
    }

    private void createFunctionalTestSuite(Project project) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        GradlePluginDevelopmentTestSuiteInternal functionalTestSuite = project.getObjects().newInstance(GradlePluginDevelopmentTestSuiteInternal.class, FUNCTIONAL_TEST_NAME, sourceSets.maybeCreate(FUNCTIONAL_TEST_NAME));
        functionalTestSuite.getTestedSourceSet().convention(project.provider(() -> sourceSets.getByName("main")));
        functionalTestSuite.getTestedGradlePlugin().set((GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware)project.getExtensions().getByType(GradlePluginDevelopmentExtension.class)).getExtensions().getByName("compatibility"));
        functionalTestSuite.getTestedGradlePlugin().disallowChanges();

        project.getComponents().add(functionalTestSuite);
        project.getExtensions().add(GradlePluginDevelopmentTestSuite.class, FUNCTIONAL_TEST_NAME, functionalTestSuite);
    }
}
