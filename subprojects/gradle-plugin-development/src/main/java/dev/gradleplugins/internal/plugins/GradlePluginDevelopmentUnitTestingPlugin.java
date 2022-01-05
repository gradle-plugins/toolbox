package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.util.HashSet;

public abstract class GradlePluginDevelopmentUnitTestingPlugin implements Plugin<Project> {
    private static final String TEST_NAME = "test";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentTestingBasePlugin.class);

        project.getPluginManager().withPlugin("dev.gradleplugins.java-gradle-plugin", appliedPlugin -> createUnitTestSuite(project));
        project.getPluginManager().withPlugin("dev.gradleplugins.groovy-gradle-plugin", appliedPlugin -> createUnitTestSuite(project));
    }

    private void createUnitTestSuite(Project project) {
        val sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        val sourceSet = sourceSets.maybeCreate(TEST_NAME);
        val factory = GradlePluginDevelopmentTestSuiteFactory.forProject(project);
        val testSuite = (GradlePluginDevelopmentTestSuiteInternal) factory.create(TEST_NAME);
        testSuite.usingSourceSet(sourceSet);
        testSuite.getTestedSourceSet().convention(project.provider(() -> sourceSets.getByName("main")));
        testSuite.getTestedGradlePlugin().set((GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware)project.getExtensions().getByType(GradlePluginDevelopmentExtension.class)).getExtensions().getByName("compatibility"));
        testSuite.getTestedGradlePlugin().disallowChanges();

        // Configure test for GradlePluginDevelopmentExtension (ensure it is not included)
        val gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        val testSourceSets = new HashSet<SourceSet>();
        testSourceSets.addAll(gradlePlugin.getTestSourceSets());
        testSourceSets.remove(sourceSet);
        gradlePlugin.testSourceSets(testSourceSets.toArray(new SourceSet[0]));

        // Automatically add Gradle API as a dependency. We assume unit tests are accomplish via ProjectBuilder
        val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.of(project.getDependencies());
        dependencies.add(testSuite.getSourceSet().getImplementationConfigurationName(), testSuite.getTestedGradlePlugin().get().getMinimumGradleVersion().map(dependencies::gradleApi));

        project.getComponents().add(testSuite);
        project.getExtensions().add(GradlePluginDevelopmentTestSuite.class, TEST_NAME, testSuite);
    }
}
