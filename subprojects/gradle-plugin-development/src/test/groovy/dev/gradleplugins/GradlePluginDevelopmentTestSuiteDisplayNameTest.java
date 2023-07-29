package dev.gradleplugins;

import dev.gradleplugins.internal.DefaultGradlePluginDevelopmentTestSuiteFactory;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GradlePluginDevelopmentTestSuiteDisplayNameTest {
    Project project = ProjectBuilder.builder().build();
    GradlePluginDevelopmentTestSuiteFactory factory = new DefaultGradlePluginDevelopmentTestSuiteFactory(project);

    @Test
    void derivesDisplayNameFromTestSuiteName() {
        assertAll(
            () -> assertEquals("tests", factory.create("test").getDisplayName()),
            () -> assertEquals("integration tests", factory.create("integrationTest").getDisplayName()),
            () -> assertEquals("functional tests", factory.create("functionalTest").getDisplayName())
        );
    }
}
