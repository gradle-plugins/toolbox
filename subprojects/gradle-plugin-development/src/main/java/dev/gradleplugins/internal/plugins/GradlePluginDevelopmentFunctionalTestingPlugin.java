package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

import java.util.HashSet;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;

public abstract class GradlePluginDevelopmentFunctionalTestingPlugin implements Plugin<Project> {
    private static final String FUNCTIONAL_TEST_NAME = "functionalTest";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("java-base");
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");

        project.getPluginManager().withPlugin("dev.gradleplugins.java-gradle-plugin", appliedPlugin -> createFunctionalTestSuite(project));
        project.getPluginManager().withPlugin("dev.gradleplugins.groovy-gradle-plugin", appliedPlugin -> createFunctionalTestSuite(project));
    }

    private void createFunctionalTestSuite(Project project) {
        val sourceSet = sourceSets(project).maybeCreate(FUNCTIONAL_TEST_NAME);
        val factory = GradlePluginDevelopmentTestSuiteFactory.forProject(project);
        val functionalTestSuite = (GradlePluginDevelopmentTestSuiteInternal) factory.create(FUNCTIONAL_TEST_NAME);
        functionalTestSuite.getSourceSet().value(sourceSet).disallowChanges();
        functionalTestSuite.getTestedSourceSet().convention(project.provider(() -> sourceSets(project).getByName("main")));
        functionalTestSuite.getTestedGradlePlugin().set(compatibility(gradlePlugin(project)));
        functionalTestSuite.getTestedGradlePlugin().disallowChanges();

        project.getPluginManager().withPlugin("dev.gradleplugins.gradle-plugin-unit-test", ignored -> {
            functionalTestSuite.getTestTasks().configureEach(task -> task.shouldRunAfter(test(project).getTestTasks().getElements()));
        });

        // Configure functionalTest for GradlePluginDevelopmentExtension
        val testSourceSets = new HashSet<SourceSet>();
        testSourceSets.addAll(gradlePlugin(project).getTestSourceSets());
        testSourceSets.add(sourceSet);
        gradlePlugin(project).testSourceSets(testSourceSets.toArray(new SourceSet[0]));

        project.getComponents().add(functionalTestSuite);
        project.getExtensions().add(GradlePluginDevelopmentTestSuite.class, FUNCTIONAL_TEST_NAME, functionalTestSuite);
    }

    private static GradlePluginDevelopmentTestSuite test(Project project) {
        return (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName("test");
    }
}
