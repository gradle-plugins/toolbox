package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.util.GradleTestUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@ExtendWith(MockitoExtension.class)
class AddGradleApiDependencyToCompileOnlyApiConfigurationUsingCompatibilityInformationIfPresentRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new AddGradleApiDependencyToCompileOnlyApiConfigurationUsingCompatibilityInformationIfPresentRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
        GradleTestUtils.setCurrentGradleVersion(GradleVersion.version("6.2.1"));
    }

    @Test
    void addsExternalGradleApiDependencyOnCompileOnlyConfigurationUsingCurrentGradleVersion() {
        subject.execute(project);

        assertThat(project.getConfigurations().getByName("compileOnly").getDependencies(),
                hasItem(coordinate("dev.gradleplugins:gradle-api:6.2.1")));
    }

    @Test
    void addsExternalGradleApiDependencyOnCompileOnlyConfigurationUsingCompatibilityGradleApiVersion() {
        ((ExtensionAware) gradlePlugin(project)).getExtensions().create("compatibility", GradlePluginDevelopmentCompatibilityExtension.class);
        compatibility(gradlePlugin(project)).getGradleApiVersion().set("6.8");
        subject.execute(project);

        assertThat(project.getConfigurations().getByName("compileOnly").getDependencies(),
                hasItem(coordinate("dev.gradleplugins:gradle-api:6.8")));
    }

    @Test
    void addsExternalGradleApiDependencyOnCompileOnlyConfigurationUsingCurrentGradleApiVersionWhenGradleApiVersionIsAbsent() {
        ((ExtensionAware) gradlePlugin(project)).getExtensions().create("compatibility", GradlePluginDevelopmentCompatibilityExtension.class);
        subject.execute(project);

        assertThat(project.getConfigurations().getByName("compileOnly").getDependencies(),
                hasItem(coordinate("dev.gradleplugins:gradle-api:6.2.1")));
    }
}
