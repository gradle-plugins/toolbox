package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.*;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GradlePluginDevelopmentBasePluginIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void appliesSubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
        project.getPluginManager().apply("java-gradle-plugin");
        compatibility(gradlePlugin(project)).getMinimumGradleVersion().set("6.5");
    }

    @Test
    void registerCompatibilityExtension() {
        assertThat(gradlePlugin(project), extensions(hasItem(allOf(named("compatibility"), publicType(GradlePluginDevelopmentCompatibilityExtension.class)))));
    }

    @Test
    void removesSelfResolvingGradleApiDependency() {
        assertThat(project.getConfigurations().getByName("api").getDependencies(), not(hasItem(isA(SelfResolvingDependency.class))));
    }

    @Test
    void addsExternalGradleApiDependencyForMinimumGradleVersionToCompileOnlyApiIfAvailable() {
        assumeTrue(project.getConfigurations().findByName("compileOnlyApi") != null);
        assertThat(project.getConfigurations().getByName("compileOnlyApi").getDependencies(),
                hasItem(allOf(isA(ExternalDependency.class), coordinate("dev.gradleplugins:gradle-api:6.5"))));
    }

    @Test
    void addsExternalGradleApiDependencyForMinimumGradleVersionToCompileOnlyIfCompileOnlyApiIsNotAvailable() {
        assumeTrue(project.getConfigurations().findByName("compileOnlyApi") == null);
        assertThat(project.getConfigurations().getByName("compileOnly").getDependencies(),
                hasItem(allOf(isA(ExternalDependency.class), coordinate("dev.gradleplugins:gradle-api:6.5"))));
    }

    @Test
    void removesAllTestSourceSetsToAvoidSelfResolvingGradleTestKitDependency() {
        assertThat(gradlePlugin(project).getTestSourceSets(), emptyIterable());
    }
}
