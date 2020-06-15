package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public abstract class GradlePluginDevelopmentUnitTestingPlugin implements Plugin<Project> {
    private static final String TEST_NAME = "test";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentTestingBasePlugin.class);

        project.getPluginManager().withPlugin("dev.gradleplugins.java-gradle-plugin", appliedPlugin -> createFunctionalTestSuite(project));
        project.getPluginManager().withPlugin("dev.gradleplugins.groovy-gradle-plugin", appliedPlugin -> createFunctionalTestSuite(project));
    }

    private void createFunctionalTestSuite(Project project) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        GradlePluginDevelopmentTestSuiteInternal testSuite = project.getObjects().newInstance(GradlePluginDevelopmentTestSuiteInternal.class, TEST_NAME, sourceSets.maybeCreate(TEST_NAME));
        testSuite.getTestedSourceSet().convention(project.provider(() -> sourceSets.getByName("main")));
        testSuite.getTestedGradlePlugin().set((GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware)project.getExtensions().getByType(GradlePluginDevelopmentExtension.class)).getExtensions().getByName("compatibility"));
        testSuite.getTestedGradlePlugin().disallowChanges();

        // Automatically add Gradle API as a dependency. We assume unit tests are accomplish via ProjectBuilder
        val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.of(project.getDependencies());
        dependencies.add(testSuite.getSourceSet().getImplementationConfigurationName(), testSuite.getTestedGradlePlugin().get().getMinimumGradleVersion().map(dependencies::gradleApi));

        project.getComponents().add(testSuite);
        project.getExtensions().add(GradlePluginDevelopmentTestSuite.class, TEST_NAME, testSuite);
    }
}
