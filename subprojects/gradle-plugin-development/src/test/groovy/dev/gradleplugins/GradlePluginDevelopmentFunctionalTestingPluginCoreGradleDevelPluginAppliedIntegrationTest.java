package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.internal.plugins.GradlePluginDevelopmentFunctionalTestingPlugin.functionalTest;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradlePluginDevelopmentFunctionalTestingPluginCoreGradleDevelPluginAppliedIntegrationTest implements GradlePluginDevelopmentTestSuiteCoreGradleDevelPluginAppliedIntegrationTester {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void appliesSubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-functional-test");
    }

    @Override
    public GradlePluginDevelopmentTestSuite subject() {
        return functionalTest(project);
    }

    @Override
    public Project project() {
        return project;
    }

    @Test
    void doesNotIncludesSourceSetInDevelTestSourceSets() {
        assertThat(gradlePlugin(project).getTestSourceSets(), not(hasItem(named("functionalTest"))));
    }

    @Test
    void hasGradleTestKitImplementationDependencyToLocalVersion() {
        assertTrue(project.getConfigurations().getByName("functionalTestImplementation").getDependencies().stream().anyMatch(localGradleTestKit()));
    }

    private static Predicate<Dependency> localGradleTestKit() {
        return dependency -> dependency instanceof SelfResolvingDependencyInternal && ((SelfResolvingDependencyInternal) dependency).getTargetComponentId().getDisplayName().equals("Gradle TestKit");
    }

    @Test
    void hasGradleTestKitImplementationDependencyToGradleApiVersion() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
        compatibility(gradlePlugin(project)).getGradleApiVersion().set("5.6");
        assertThat(project.getConfigurations().getByName("functionalTestImplementation").getDependencies(), hasItem(coordinate("dev.gradleplugins:gradle-test-kit:5.6")));
    }
}
