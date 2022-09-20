package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.capabilities;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class VersionedSourceSet_RegisterJvmFeatureForEachVersionedSourceSetRuleIntegrationTests {
    Project project = ProjectBuilder.builder().withName("foo").build();
    Action<Project> subject = new VersionedSourceSet_RegisterJvmFeatureForEachVersionedSourceSetRule();

    @BeforeEach
    void configureProject() {
        project.setGroup("com.example");
        project.setVersion("4.2");
        project.getPluginManager().apply("java-base");
        subject.execute(project);
    }

    @Test
    void registersFeatureForEachVersionedSourceSetWithCustomCapability() {
        sourceSets(project, it -> it.create("v5.6"));

        assertThat(project.getConfigurations().getByName("v56ApiElements").getOutgoing(),
                capabilities(hasItem(coordinate("com.example:foo-gradle-v5.6:4.2"))));
    }
}
