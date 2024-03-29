package dev.gradleplugins;

import dev.gradleplugins.internal.DefaultGradlePluginDevelopmentTestSuiteFactory;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.FileMatchers.aFile;
import static dev.gradleplugins.FileMatchers.withAbsolutePath;
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
    GradlePluginDevelopmentTestSuiteFactory factory = new DefaultGradlePluginDevelopmentTestSuiteFactory(project);
    GradlePluginDevelopmentTestSuite testSuite = factory.create("etreTest");
    PluginUnderTestMetadata subject = testSuite.getPluginUnderTestMetadataTask().get();

    @BeforeEach
    void configureSourceSet() {
        project.getPluginManager().apply("java-base");
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
