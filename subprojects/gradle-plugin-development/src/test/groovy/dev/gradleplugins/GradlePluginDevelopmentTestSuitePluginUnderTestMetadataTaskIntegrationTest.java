package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.file.Directory;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.FileMatchers.aFile;
import static dev.gradleplugins.FileMatchers.withAbsolutePath;
import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class GradlePluginDevelopmentTestSuitePluginUnderTestMetadataTaskIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite testSuite = factory.create("etreTest");
    private final PluginUnderTestMetadata subject = testSuite.getPluginUnderTestMetadataTask().get();

    @BeforeEach
    void configureSourceSet() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        project.getPluginManager().apply("java-base");
        testSuite.finalizeComponent();
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
    void configuresOutputDirectoryConventionToBuildDirectoryByTaskName() {
        assertThat(subject.getOutputDirectory().value((Directory) null),
                providerOf(aFile(withAbsolutePath(endsWith("/build/pluginUnderTestMetadataEtreTest")))));
    }

    @Test
    void addsPluginUnderTestMetadataAsRuntimeOnlyDependency() {
        assertThat(project.getConfigurations().getByName("etreTestRuntimeOnly").getDependencies(),
                hasItem(isA(SelfResolvingDependency.class)));
    }

    @Test
    void includesPluginUnderTestMetadataConfigurationDependencies() {
        testSuite.getDependencies().pluginUnderTestMetadata(project.files("my/own/dep.jar"));
        assertThat(subject.getPluginClasspath(), contains(aFile(withAbsolutePath(endsWith("/my/own/dep.jar")))));
    }
}
