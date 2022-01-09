package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

class GradlePluginDevelopmentFunctionalTestingPluginIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();

    @BeforeEach
    void appliesSubjectPlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-functional-test");
    }

    @Test
    void registersFunctionalTestExtensionAsTestSuite() {
        assertThat(project, extensions(hasItem(allOf(named("functionalTest"), publicType(GradlePluginDevelopmentTestSuite.class)))));
    }

    @Test
    void appliesJavaBasePlugin() {
        assertThat(project, hasPlugin("java-base"));
    }

    @Test
    void appliesTestingBasePlugin() {
        assertThat(project, hasPlugin("dev.gradleplugins.gradle-plugin-testing-base"));
    }
}
