package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GradlePluginDevelopmentTestSuiteDisplayNameTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);

    @Test
    void derivesDisplayNameFromTestSuiteName() {
        assertAll(
            () -> assertEquals("tests", factory.create("test").getDisplayName()),
            () -> assertEquals("integration tests", factory.create("integrationTest").getDisplayName()),
            () -> assertEquals("functional tests", factory.create("functionalTest").getDisplayName())
        );
    }
}
