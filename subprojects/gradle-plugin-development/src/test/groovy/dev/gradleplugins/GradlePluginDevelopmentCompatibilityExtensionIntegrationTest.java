package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GradlePluginDevelopmentCompatibilityExtensionIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private GradlePluginDevelopmentCompatibilityExtension subject;

    @BeforeEach
    void setup() {
        project.getPluginManager().apply("dev.gradleplugins.base");
        project.getPluginManager().apply("java-gradle-plugin");
        subject = compatibility(gradlePlugin(project));
    }

    @Test
    void hasMinimumGradleVersion() {
        assertNotNull(subject.getMinimumGradleVersion());
    }

    @Test
    void hasGradleApiVersion() {
        assertNotNull(subject.getGradleApiVersion());
    }

    @Test
    void defaultsGradleApiToMinimumGradleVersion() {
        subject.getMinimumGradleVersion().set("6.9");
        subject.getGradleApiVersion().value((String) null);
        assertThat(subject.getGradleApiVersion(), providerOf("6.9"));
    }

    @Test
    void defaultsGradleApiToLocalWhenMinimumGradleVersionIsSnapshot() {
        subject.getMinimumGradleVersion().set("7.5-20220105231256+0000");
        subject.getGradleApiVersion().value((String) null);
        assertThat(subject.getGradleApiVersion(), providerOf("local"));
    }
}
