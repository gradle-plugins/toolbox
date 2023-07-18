package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.publicType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradlePluginDevelopmentDependencyExtensionTest {
    static Project project;
    static GradlePluginDevelopmentDependencyExtension subject;

    @BeforeAll
    static void givenExtendedProject() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-development");
        subject = GradlePluginDevelopmentDependencyExtension.from(project.getDependencies());
    }

    @Test
    void canUseLocalGradleApiDependency() {
        Dependency dependency = subject.gradleApi("local");
        assertThat(dependency, isA(SelfResolvingDependency.class));

        ComponentIdentifier targetComponentId = ((SelfResolvingDependencyInternal) dependency).getTargetComponentId();
        assertThat(targetComponentId, notNullValue());
        assertThat(targetComponentId.getDisplayName(), equalTo("Gradle API"));
    }

    @Test
    void canUseLocalGradleTestKitDependency() {
        Dependency dependency = subject.gradleTestKit("local");
        assertThat(dependency, isA(SelfResolvingDependency.class));

        ComponentIdentifier targetComponentId = ((SelfResolvingDependencyInternal) dependency).getTargetComponentId();
        assertThat(targetComponentId, notNullValue());
        assertThat(targetComponentId.getDisplayName(), equalTo("Gradle TestKit"));
    }

    @Test
    void throwsNullPointerExceptionWhenHandlerForExtensionIsNull() {
        assertThrows(NullPointerException.class, () -> GradlePluginDevelopmentDependencyExtension.from(null));
    }

    @Test
    void usesSpecificGradleApiDependencyVersion() {
        Dependency dependency = subject.gradleApi("6.2.1");
        assertThat(dependency, isA(ExternalDependency.class));
        assertThat(dependency.getGroup(), equalTo("dev.gradleplugins"));
        assertThat(dependency.getName(), equalTo("gradle-api"));
        assertThat(dependency.getVersion(), equalTo("6.2.1"));
    }

    @Test
    void throwsNullPointerExceptionWhenGradleApiVersionIsNull() {
        assertThrows(NullPointerException.class, () -> subject.gradleApi(null));
    }

    @Test
    void usesSpecificGradleTestKitDependencyVersion() {
        Dependency dependency = subject.gradleTestKit("6.2.1");
        assertThat(dependency, isA(ExternalDependency.class));
        assertThat(dependency.getGroup(), equalTo("dev.gradleplugins"));
        assertThat(dependency.getName(), equalTo("gradle-test-kit"));
        assertThat(dependency.getVersion(), equalTo("6.2.1"));
    }

    @Test
    void throwsNullPointerExceptionWhenGradleTestKitVersionIsNull() {
        assertThrows(NullPointerException.class, () -> subject.gradleTestKit(null));
    }

    @Test
    void canCreateGradleRunnerKitDependency() {
        Dependency dependency = subject.gradleRunnerKit();
        assertThat(dependency, isA(ExternalDependency.class));
        assertThat(dependency.getGroup(), equalTo("dev.gradleplugins"));
        assertThat(dependency.getName(), equalTo("gradle-runner-kit"));
        assertThat(dependency.getVersion(), notNullValue());
    }

    @Test
    void canCreateGradleFixturesDependency() {
        Dependency dependency = subject.gradleFixtures();
        assertThat(dependency, isA(ExternalDependency.class));
        assertThat(dependency.getGroup(), equalTo("dev.gradleplugins"));
        assertThat(dependency.getName(), equalTo("gradle-fixtures"));
        assertThat(dependency.getVersion(), notNullValue());
    }

    @Test
    void hasPublicType() {
        assertThat(subject, publicType(GradlePluginDevelopmentDependencyExtension.class));
    }
}
