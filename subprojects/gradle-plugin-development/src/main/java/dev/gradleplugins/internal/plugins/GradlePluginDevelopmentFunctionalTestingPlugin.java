package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
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

        project.getPluginManager().withPlugin("dev.gradleplugins.gradle-plugin-unit-test", ignored -> {
            functionalTest(project).getTestTasks().configureEach(task -> task.shouldRunAfter(test(project).getTestTasks().getElements()));
        });

        functionalTest(project).dependencies(dependencies -> {
            dependencies.getImplementation().add(project.provider(() -> {
                if (project.getPluginManager().hasPlugin("java-gradle-plugin")) {
                    return compatibility(gradlePlugin(project)).getGradleApiVersion().getOrElse("local");
                }
                return "local";
            }).map(dependencies::gradleTestKit));
        });
    }
}
