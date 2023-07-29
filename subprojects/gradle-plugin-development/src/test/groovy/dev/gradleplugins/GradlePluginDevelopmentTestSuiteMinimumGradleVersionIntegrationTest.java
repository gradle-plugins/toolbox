package dev.gradleplugins;

import dev.gradleplugins.internal.DefaultGradlePluginDevelopmentTestSuiteFactory;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.ProjectMatchers.absentProvider;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradlePluginDevelopmentTestSuiteMinimumGradleVersionIntegrationTest {
    Project project = ProjectBuilder.builder().build();
    GradlePluginDevelopmentTestSuiteFactory factory = new DefaultGradlePluginDevelopmentTestSuiteFactory(project);
    GradlePluginDevelopmentTestSuite subject = factory.create("holu");

    @BeforeEach
    void appliesBasePlugin() {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-development");
    }

    @Test
    void throwsExceptionOnCoverageForMinimumVersionQuery() {
        final Throwable ex = assertThrows(RuntimeException.class, () -> subject.getStrategies().getCoverageForMinimumVersion().getVersion());
        assertEquals("Cannot query the value of property 'minimumGradleVersion' because it has no value available.", ex.getMessage());
    }

    @Test
    void returnsGroovyDependencyForCurrentGradleVersion() {
        assertThat(subject.getDependencies().groovy(), coordinate("org.codehaus.groovy:groovy-all:2.5.8"));
    }

    @Nested
    class FinalizeTestedDevelComponentTest {
        @BeforeEach
        void appliesCoreDevelComponentPlugins() {
            project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
            project.getPluginManager().apply("java-gradle-plugin");
        }

        @Test
        void calculatesDefaultCompatibilityMinimumGradleVersionOnDevelWhenTestingStrategyQueried() {
            assertThat(subject.getStrategies().getCoverageForMinimumVersion().getVersion(), equalTo(GradleVersion.current().getVersion()));
            assertThat(compatibility(gradlePlugin(project)).getMinimumGradleVersion(), absentProvider());
        }

        @Test
        void finalizesCompatibilityExtensionWhenTestingStrategyQueried() {
            subject.getStrategies().getCoverageForMinimumVersion().getVersion();
            assertThrows(RuntimeException.class, () -> compatibility(gradlePlugin(project)).getMinimumGradleVersion().set("n/a"));
        }
    }

    @Nested
    class TestedDevelComponentTest {
        @BeforeEach
        void appliesCoreDevelComponentPlugins() {
            project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
            project.getPluginManager().apply("java-gradle-plugin");
            compatibility(gradlePlugin(project)).getMinimumGradleVersion().set("4.9");
        }

        @Test
        void returnsDevelCompatibilityMinimumGradleVersion() {
            assertThat(subject.getStrategies().getCoverageForMinimumVersion().getVersion(), equalTo("4.9"));
        }

        @Test
        void returnsGroovyDependencyForMinimumGradleVersion() {
            assertThat(subject.getDependencies().groovy(), coordinate("org.codehaus.groovy:groovy-all:2.4.12"));
        }
    }
}
