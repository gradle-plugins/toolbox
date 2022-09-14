package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;

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
        // See https://github.com/gradle-plugins/toolbox/issues/65 for more information
        project.getPluginManager().apply("java"); // Should be java-base
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        TEST_RULE.execute(project);

        project.getPluginManager().withPlugin("java-gradle-plugin", ignored -> {
            project.getPluginManager().withPlugin("dev.gradleplugins.gradle-plugin-base", useGradleApiImplementationDependency(project));
        });
    }

    private static Action<AppliedPlugin> useGradleApiImplementationDependency(Project project) {
        return ignored -> {
            // Automatically add Gradle API as a dependency. We assume unit tests are accomplished via ProjectBuilder
            val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.of(project.getDependencies());
            dependencies.add(test(project).getSourceSet().get().getImplementationConfigurationName(), project.provider(() -> dependencies.gradleApi(compatibility(gradlePlugin(project)).getGradleApiVersion().get())));
        };
    }
}
