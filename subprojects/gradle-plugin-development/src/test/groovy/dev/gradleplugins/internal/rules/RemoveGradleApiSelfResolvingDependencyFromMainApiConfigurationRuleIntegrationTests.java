package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;

class RemoveGradleApiSelfResolvingDependencyFromMainApiConfigurationRuleIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    Action<Project> subject = new RemoveGradleApiSelfResolvingDependencyFromMainApiConfigurationRule();

    @BeforeEach
    void configureProject() {
        project.getPluginManager().apply("java-gradle-plugin");
    }

    @Test
    void removesLocalGradleApiDependencyFromApiConfiguration() {
        subject.execute(project);
        assertThat(project.getConfigurations().getByName("api").getDependencies(),
                not(hasItem(isA(SelfResolvingDependency.class))));
    }
}
