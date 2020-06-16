package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.util.HashSet;

public abstract class GradlePluginDevelopmentFunctionalTestingPlugin implements Plugin<Project> {
    private static final String FUNCTIONAL_TEST_NAME = "functionalTest";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentTestingBasePlugin.class);

        project.getPluginManager().withPlugin("dev.gradleplugins.java-gradle-plugin", appliedPlugin -> createFunctionalTestSuite(project));
        project.getPluginManager().withPlugin("dev.gradleplugins.groovy-gradle-plugin", appliedPlugin -> createFunctionalTestSuite(project));
    }

    private void createFunctionalTestSuite(Project project) {
        val sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        val sourceSet = sourceSets.maybeCreate(FUNCTIONAL_TEST_NAME);
        val functionalTestSuite = project.getObjects().newInstance(GradlePluginDevelopmentTestSuiteInternal.class, FUNCTIONAL_TEST_NAME, sourceSet);
        functionalTestSuite.getTestedSourceSet().convention(project.provider(() -> sourceSets.getByName("main")));
        functionalTestSuite.getTestedGradlePlugin().set((GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware)project.getExtensions().getByType(GradlePluginDevelopmentExtension.class)).getExtensions().getByName("compatibility"));
        functionalTestSuite.getTestedGradlePlugin().disallowChanges();

        // Configure functionalTest for GradlePluginDevelopmentExtension
        val gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        val testSourceSets = new HashSet<SourceSet>();
        testSourceSets.addAll(gradlePlugin.getTestSourceSets());
        testSourceSets.add(sourceSet);
        gradlePlugin.testSourceSets(testSourceSets.toArray(new SourceSet[0]));

        project.getComponents().add(functionalTestSuite);
        project.getExtensions().add(GradlePluginDevelopmentTestSuite.class, FUNCTIONAL_TEST_NAME, functionalTestSuite);
    }
}
