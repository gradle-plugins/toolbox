package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.absentProvider;
import static dev.gradleplugins.ProjectMatchers.extensions;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.publicType;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.apiSourceSet;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiSourceSet_RegisterApiSourceSetPropertyOnGradlePluginDevelopmentExtensionRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new ApiSourceSet_RegisterApiSourceSetPropertyOnGradlePluginDevelopmentExtensionRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
    }

    @Test
    void registersPropertyForApiSourceSetOnGradleDevelopmentExtension() {
        subject.execute(project);
        assertThat(gradlePlugin(project), extensions(hasItem(allOf(
                named("apiSourceSet"), publicType(new TypeOf<Property<SourceSet>>() {})))));
    }

    @Test
    void hasNoDefaultValue() {
        subject.execute(project);
        assertThat(apiSourceSet(gradlePlugin(project)), absentProvider());
    }

    @Test
    void finalizeValueOnRead() {
        subject.execute(project);
        apiSourceSet(gradlePlugin(project)).value(sourceSets(project).map(it -> it.maybeCreate("my-api"))).get();
        assertThrows(IllegalStateException.class, () -> apiSourceSet(gradlePlugin(project)).set((SourceSet) null));
    }
}
