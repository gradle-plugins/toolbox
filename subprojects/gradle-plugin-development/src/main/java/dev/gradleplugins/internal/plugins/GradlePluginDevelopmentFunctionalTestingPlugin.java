package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

import java.util.HashSet;

import static dev.gradleplugins.internal.plugins.GradlePluginDevelopmentUnitTestingPlugin.test;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

public abstract class GradlePluginDevelopmentFunctionalTestingPlugin implements Plugin<Project> {
    private static final String FUNCTIONAL_TEST_NAME = "functionalTest";
    private static final GradlePluginDevelopmentTestSuiteRegistrationAction FUNCTIONAL_TEST_RULE = new GradlePluginDevelopmentTestSuiteRegistrationAction(FUNCTIONAL_TEST_NAME);

    public static GradlePluginDevelopmentTestSuite functionalTest(Project project) {
        return (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName(FUNCTIONAL_TEST_NAME);
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("java-base");
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        FUNCTIONAL_TEST_RULE.execute(project);

        project.getPluginManager().withPlugin("java-gradle-plugin", ignored -> {
            // Configure functionalTest for GradlePluginDevelopmentExtension
            val testSourceSets = new HashSet<SourceSet>();
            testSourceSets.addAll(gradlePlugin(project).getTestSourceSets());
            testSourceSets.add(functionalTest(project).getSourceSet().get());
            gradlePlugin(project).testSourceSets(testSourceSets.toArray(new SourceSet[0]));
        });
        project.getPluginManager().withPlugin("dev.gradleplugins.gradle-plugin-unit-test", ignored -> {
            functionalTest(project).getTestTasks().configureEach(task -> task.shouldRunAfter(test(project).getTestTasks().getElements()));
        });
    }
}
