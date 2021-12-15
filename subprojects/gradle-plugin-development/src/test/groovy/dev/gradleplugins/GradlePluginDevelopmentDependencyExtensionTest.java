package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradlePluginDevelopmentDependencyExtensionTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentDependencyExtension subject = GradlePluginDevelopmentDependencyExtension.from(project.getDependencies());

    @Test
    void canUseLocalGradleApiDependency() {
        Dependency dependency = subject.gradleApi("local");
        assertTrue(dependency instanceof SelfResolvingDependency);
        assertEquals("Gradle API", ((SelfResolvingDependencyInternal) dependency).getTargetComponentId().getDisplayName());
    }

    @Test
    void canUseLocalGradleTestKitDependency() {
        Dependency dependency = subject.gradleTestKit("local");
        assertTrue(dependency instanceof SelfResolvingDependency);
        assertEquals("Gradle TestKit", ((SelfResolvingDependencyInternal) dependency).getTargetComponentId().getDisplayName());
    }

    @Test
    void throwsNullPointerExceptionWhenHandlerForExtensionIsNull() {
        assertThrows(NullPointerException.class, () -> GradlePluginDevelopmentDependencyExtension.from(null));
    }

    @Test
    void usesSpecificGradleApiDependencyVersion() {
        Dependency dependency = subject.gradleApi("6.2.1");
        assertTrue(dependency instanceof ExternalDependency);
        assertEquals("dev.gradleplugins", dependency.getGroup());
        assertEquals("gradle-api", dependency.getName());
        assertEquals("6.2.1", dependency.getVersion());
    }

    @Test
    void throwsNullPointerExceptionWhenGradleApiVersionIsNull() {
        assertThrows(NullPointerException.class, () -> subject.gradleApi(null));
    }

    @Test
    void usesSpecificGradleTestKitDependencyVersion() {
        Dependency dependency = subject.gradleTestKit("6.2.1");
        assertTrue(dependency instanceof ExternalDependency);
        assertEquals("dev.gradleplugins", dependency.getGroup());
        assertEquals("gradle-test-kit", dependency.getName());
        assertEquals("6.2.1", dependency.getVersion());
    }

    @Test
    void throwsNullPointerExceptionWhenGradleTestKitVersionIsNull() {
        assertThrows(NullPointerException.class, () -> subject.gradleTestKit(null));
    }

    @Test
    void canCreateGradleRunnerKitDependency() {
        Dependency dependency = subject.gradleRunnerKit();
        assertTrue(dependency instanceof ExternalDependency);
        assertEquals("dev.gradleplugins", dependency.getGroup());
        assertEquals("gradle-runner-kit", dependency.getName());
        assertNotNull(dependency.getVersion());
    }

    @Test
    void canCreateGradleFixturesDependency() {
        Dependency dependency = subject.gradleFixtures();
        assertTrue(dependency instanceof ExternalDependency);
        assertEquals("dev.gradleplugins", dependency.getGroup());
        assertEquals("gradle-fixtures", dependency.getName());
        assertNotNull(dependency.getVersion());
    }
}
