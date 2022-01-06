package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.internal.CreateTestTasksFromTestingStrategiesRule;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import dev.gradleplugins.internal.TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

abstract class GradlePluginDevelopmentTestingBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentTestingBasePlugin() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java-gradle-plugin", new RegisterTestingExtensionOnGradleDevelExtensionRule(project));
        project.getComponents().withType(GradlePluginDevelopmentTestSuiteInternal.class).configureEach(testSuite -> {
            testSuite.getTestTasks().configureEach(task -> {
                val testingStrategy = project.getObjects().property(GradlePluginTestingStrategy.class);
                task.getExtensions().add(new TypeOf<Property<GradlePluginTestingStrategy>>() {}, "testingStrategy", testingStrategy);
            });
            project.afterEvaluate(proj -> {
                testSuite.finalizeComponent();
            });
        });
    }
}
