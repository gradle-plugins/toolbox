package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

import java.util.HashSet;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;

public abstract class GradlePluginDevelopmentUnitTestingPlugin implements Plugin<Project> {
    private static final String TEST_NAME = "test";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");

        project.getPluginManager().withPlugin("dev.gradleplugins.java-gradle-plugin", appliedPlugin -> createUnitTestSuite(project));
        project.getPluginManager().withPlugin("dev.gradleplugins.groovy-gradle-plugin", appliedPlugin -> createUnitTestSuite(project));
    }

    private void createUnitTestSuite(Project project) {
        val sourceSet = sourceSets(project).maybeCreate(TEST_NAME);
        val factory = GradlePluginDevelopmentTestSuiteFactory.forProject(project);
        val testSuite = (GradlePluginDevelopmentTestSuiteInternal) factory.create(TEST_NAME);
        testSuite.getSourceSet().value(sourceSet).disallowChanges();
        testSuite.getTestedSourceSet().convention(project.provider(() -> sourceSets(project).getByName("main")));
        testSuite.getTestedGradlePlugin().set(compatibility(gradlePlugin(project)));
        testSuite.getTestedGradlePlugin().disallowChanges();

        // Configure test for GradlePluginDevelopmentExtension (ensure it is not included)
        val testSourceSets = new HashSet<SourceSet>();
        testSourceSets.addAll(gradlePlugin(project).getTestSourceSets());
        testSourceSets.remove(sourceSet);
        gradlePlugin(project).testSourceSets(testSourceSets.toArray(new SourceSet[0]));

        // Automatically add Gradle API as a dependency. We assume unit tests are accomplish via ProjectBuilder
        val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.of(project.getDependencies());
        dependencies.add(sourceSet.getImplementationConfigurationName(), testSuite.getTestedGradlePlugin().get().getMinimumGradleVersion().map(dependencies::gradleApi));

        project.getComponents().add(testSuite);
        project.getExtensions().add(GradlePluginDevelopmentTestSuite.class, TEST_NAME, testSuite);
    }
}
