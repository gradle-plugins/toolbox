package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.*;
import static dev.gradleplugins.internal.plugins.GradlePluginDevelopmentUnitTestingPlugin.test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

class GradlePluginDevelopmentUnitTestingPluginIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void appliesSubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-unit-test");
    }

    @Test
    void registersTestExtensionAsTestSuite() {
        assertThat(project, extensions(hasItem(allOf(named("test"), publicType(GradlePluginDevelopmentTestSuite.class)))));
    }

    @Test
    void appliesJavaBasePlugin() {
        // Despite the fact that we apply the 'java' plugin, our intention is to apply only 'java-base.
        // See https://github.com/gradle-plugins/toolbox/issues/65 and the related tests
        assertThat(project, hasPlugin("java-base"));
    }

    @Test
    void appliesTestingBasePlugin() {
        assertThat(project, hasPlugin("dev.gradleplugins.gradle-plugin-testing-base"));
    }

    @Nested
    class GradlePluginTestSuiteTest {
        public GradlePluginDevelopmentTestSuite subject() {
            return test(project);
        }

        @Test
        void disallowChangesToSourceSetProperty() {
            final Throwable ex = assertThrows(RuntimeException.class, () -> subject().getSourceSet().set((SourceSet) null));
            assertEquals("The value for test suite 'test' property 'sourceSet' cannot be changed any further.", ex.getMessage());
        }

        @Test // https://github.com/gradle-plugins/toolbox/issues/65
        void canAddDependenciesBeforeCoreGradleDevelPluginApplied() {
            // We need to avoid an implementation that rely on the sourceSet for the component dependencies.
            // If we do, the sourceSet can realize/register before we apply core Gradle plugins.
            // Those plugins assume the sourceSet does not exist which result in a failure.
            subject().getDependencies().implementation("org.junit.jupiter:junit-jupiter:5.8.1");
            assertThat(project.getConfigurations().getByName("testImplementation").getDependencies(), hasItem(coordinate("org.junit.jupiter:junit-jupiter:5.8.1")));
            assertDoesNotThrow(() -> project.getPluginManager().apply("java-gradle-plugin"));
        }
    }
}
