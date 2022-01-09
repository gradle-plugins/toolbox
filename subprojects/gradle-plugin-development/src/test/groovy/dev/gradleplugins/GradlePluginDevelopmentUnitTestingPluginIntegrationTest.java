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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    }
}
