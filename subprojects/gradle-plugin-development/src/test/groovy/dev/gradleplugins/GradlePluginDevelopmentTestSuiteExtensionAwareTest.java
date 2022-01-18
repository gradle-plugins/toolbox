package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradlePluginDevelopmentTestSuiteExtensionAwareTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("poly");

    @Test
    void canAccessExtensionContainerAtCompileTime() {
        assertNotNull(subject.getExtensions()); // compile-time check
    }

    @Test
    void isExtensionAware() {
        // Ignore IntelliJ's suggestion for the below assertion.
        //  There is a clear distinction between being ExtensionAware and extending ExtensionAware.
        //  Here we test being ExtensionAware which is normally true for all types created via ObjectFactory.
        //  The other test check for extending ExtensionAware via compile-time check.
        assertTrue(subject instanceof ExtensionAware);
    }
}
