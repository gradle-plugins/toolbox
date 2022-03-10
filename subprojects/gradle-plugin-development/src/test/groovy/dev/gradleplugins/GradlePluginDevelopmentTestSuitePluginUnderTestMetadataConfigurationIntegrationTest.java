package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.tasks.SourceSet;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class GradlePluginDevelopmentTestSuitePluginUnderTestMetadataConfigurationIntegrationTest {
    private final Project project = ProjectBuilder.builder().withName("ople").build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("loki");

    @BeforeEach
    void configureSourceSet() {
        project.setGroup("com.example");
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

    @Nested
    class PluginUnderTestMetadataConfigurationTest {
        Configuration subject() {
            return subject.getDependencies().getPluginUnderTestMetadata().get();
        }

        @Test
        void isResolvableDependencyBucket() {
            assertFalse(subject().isCanBeConsumed());
            assertTrue(subject().isCanBeResolved());
        }

        @Test
        void hasDescription() {
            assertThat(subject().getDescription(), equalTo("Plugin under test metadata for source set 'koli'."));
        }

        @Test
        void hasJavaRuntimeUsageAttribute() {
            assertThat(subject().getAttributes().getAttribute(Usage.USAGE_ATTRIBUTE), named("java-runtime"));
        }
    }
}
