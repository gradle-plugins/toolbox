package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class GradlePluginDevelopmentTestingExtensionRegisterSuiteIntegrationTest implements GradlePluginDevelopmentTestSuiteTester {
    private final Project project = ProjectBuilder.builder().build();
    private GradlePluginDevelopmentTestSuite subject;
    
    @BeforeEach
    void setup() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        project.getPluginManager().apply("java-gradle-plugin");
        subject = ((ExtensionAware) project.getExtensions().getByType(GradlePluginDevelopmentExtension.class))
                .getExtensions().getByType(GradlePluginDevelopmentTestingExtension.class).registerSuite("hoke");
    }

    @Override
    public GradlePluginDevelopmentTestSuite subject() {
        return subject;
    }

    @Test
    public void hasName() {
        assertThat(subject, named("hoke"));
    }

    @Test
    void addsTestSuiteToSoftwareComponents() {
        assertThat(project.getComponents(), hasItem(allOf(named("hoke"), isA(GradlePluginDevelopmentTestSuite.class))));
    }
}
