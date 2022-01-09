package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

import java.util.HashSet;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

public abstract class GradlePluginDevelopmentUnitTestingPlugin implements Plugin<Project> {
    private static final String TEST_NAME = "test";
    private static final GradlePluginDevelopmentTestSuiteRegistrationAction TEST_RULE = new GradlePluginDevelopmentTestSuiteRegistrationAction(TEST_NAME);

    public static GradlePluginDevelopmentTestSuite test(Project project) {
        return (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName("test");
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("java-base");
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        TEST_RULE.execute(project);

        project.getPluginManager().withPlugin("dev.gradleplugins.java-gradle-plugin", appliedPlugin -> createUnitTestSuite(project));
        project.getPluginManager().withPlugin("dev.gradleplugins.groovy-gradle-plugin", appliedPlugin -> createUnitTestSuite(project));
        project.getPluginManager().withPlugin("java-gradle-plugin", ignored -> {
            // Configure test for GradlePluginDevelopmentExtension (ensure it is not included)
            val testSourceSets = new HashSet<SourceSet>();
            testSourceSets.addAll(gradlePlugin(project).getTestSourceSets());
            testSourceSets.remove(test(project).getSourceSet().get());
            gradlePlugin(project).testSourceSets(testSourceSets.toArray(new SourceSet[0]));
        });
    }

    private void createUnitTestSuite(Project project) {
        val testSuite = (GradlePluginDevelopmentTestSuiteInternal) test(project);
        testSuite.getTestedGradlePlugin().set(compatibility(gradlePlugin(project)));
        testSuite.getTestedGradlePlugin().disallowChanges();

        // Automatically add Gradle API as a dependency. We assume unit tests are accomplish via ProjectBuilder
        val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.of(project.getDependencies());
        dependencies.add(testSuite.getSourceSet().get().getImplementationConfigurationName(), testSuite.getTestedGradlePlugin().get().getMinimumGradleVersion().map(dependencies::gradleApi));
    }
}
