package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class GradlePluginDevelopmentTestSuitePluginUnderTestMetadataIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("loki");

    @BeforeEach
    void configureSourceSet() {
        project.getPluginManager().apply("java-base");
        subject.getSourceSet().set(sourceSets(project).create("koli"));
    }

    @Test
    void doesNotCreateConfigurationPrefixedByTestSuiteName() {
        assertThat(project.getConfigurations(), everyItem(not(named("lokiPluginUnderTestMetadata"))));
    }

    @Test
    void usesConfigurationBasedOnTestSuiteSourceSet() {
        subject.getDependencies().pluginUnderTestMetadata("com.example:some-plugin:4.2");
        assertThat(project.getConfigurations(), hasItem(named("koliPluginUnderTestMetadata")));
        assertThat(project.getConfigurations().getByName("koliPluginUnderTestMetadata").getDependencies(),
                hasItem(coordinate("com.example:some-plugin:4.2")));
    }

    @Test
    void finalizeSourceSetPropertyWhenPluginUnderTestMetadataDependency() {
        subject.getDependencies().pluginUnderTestMetadata("com.example:some-other-plugin:4.2");
        assertThrows(RuntimeException.class, () -> subject.getSourceSet().set(mock(SourceSet.class)));
    }
}
