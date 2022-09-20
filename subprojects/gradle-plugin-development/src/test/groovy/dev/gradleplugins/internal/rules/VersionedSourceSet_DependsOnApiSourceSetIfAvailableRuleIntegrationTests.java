package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;

class VersionedSourceSet_DependsOnApiSourceSetIfAvailableRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new VersionedSourceSet_DependsOnApiSourceSetIfAvailableRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
    }
}
