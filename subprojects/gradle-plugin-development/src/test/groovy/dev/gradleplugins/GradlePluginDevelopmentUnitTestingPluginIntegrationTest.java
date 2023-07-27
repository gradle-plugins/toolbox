package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.extensions;
import static dev.gradleplugins.ProjectMatchers.hasPlugin;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.publicType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

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
}
