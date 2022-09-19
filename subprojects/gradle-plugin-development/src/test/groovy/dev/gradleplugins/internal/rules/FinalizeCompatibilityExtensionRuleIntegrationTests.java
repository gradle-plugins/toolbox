package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinalizeCompatibilityExtensionRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new FinalizeCompatibilityExtensionRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
        ((ExtensionAware) gradlePlugin(project)).getExtensions().create("compatibility", GradlePluginDevelopmentCompatibilityExtension.class);
    }

    @Test
    void disallowChangesOnMinimumGradleVersionProperty() {
        subject.execute(project);
        assertThrows(IllegalStateException.class,
                () -> compatibility(gradlePlugin(project)).getMinimumGradleVersion().set((String) null));
    }

    @Test
    void disallowChangesOnGradleApiVersionProperty() {
        subject.execute(project);
        assertThrows(IllegalStateException.class,
                () -> compatibility(gradlePlugin(project)).getGradleApiVersion().set((String) null));
    }
}
