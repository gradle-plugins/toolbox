package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.FileMatchers.aFile;
import static dev.gradleplugins.FileMatchers.withAbsolutePath;
import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

class GradlePluginDevelopmentTestSuitePluginUnderTestMetadataTaskIntegrationTest {
    Project project = ProjectBuilder.builder().build();
    GradlePluginDevelopmentTestSuiteFactory factory;
    GradlePluginDevelopmentTestSuite testSuite;
    PluginUnderTestMetadata subject;

    @BeforeEach
    void configureSourceSet() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        project.getPluginManager().apply("java-base");
        factory = forProject(project);
        testSuite = factory.create("etreTest");
        subject = testSuite.getPluginUnderTestMetadataTask().get();
    }

    @Test
    void hasPluginDevelopmentGroup() {
        assertThat(subject.getGroup(), equalTo("Plugin development"));
    }

    @Test
    void hasDescription() {
        assertThat(subject.getDescription(), allOf(startsWith("Generates the metadata for plugin"), endsWith(".")));
    }

    @Test
    void usesDisplayNameInTaskDescription() {
        assertThat(subject.getDescription(), containsString("etre tests"));
    }

    @Test
    void createsTestSuiteSpecificPluginUnderTestMetadataTask() {
        assertThat(subject, named("pluginUnderTestMetadataEtreTest"));
    }

    @Test
    @Disabled
    void configuresOutputDirectoryConventionToBuildDirectoryByTaskName() {
        assertThat(subject.getOutputDirectory().value((Directory) null),
                providerOf(aFile(withAbsolutePath(endsWith("/build/pluginUnderTestMetadataEtreTest")))));
    }
}
