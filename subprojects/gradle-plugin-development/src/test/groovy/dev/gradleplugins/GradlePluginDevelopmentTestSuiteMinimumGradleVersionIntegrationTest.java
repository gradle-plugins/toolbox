package dev.gradleplugins;

import dev.gradleplugins.internal.util.GradleTestUtils;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static dev.gradleplugins.ProjectMatchers.coordinate;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradlePluginDevelopmentTestSuiteMinimumGradleVersionIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("holu");

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
        assertThat(subject.getDependencies().groovy(), providerOf(coordinate("org.codehaus.groovy:groovy-all:2.5.8")));
    }

    @Nested
    class FinalizeTestedDevelComponentTest {
        @BeforeEach
        void appliesCoreDevelComponentPlugins() {
            project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
            project.getPluginManager().apply("java-gradle-plugin");
            GradleTestUtils.setCurrentGradleVersion(GradleVersion.version("6.2.1"));
            ((ProjectInternal) project).evaluate();
        }

        @Test
        void calculatesDefaultCompatibilityMinimumGradleVersionOnDevelWhenTestingStrategyQueried() {
            assertThat(subject.getStrategies().getCoverageForMinimumVersion().getVersion(), equalTo(GradleVersion.current().getVersion()));
            assertThat(compatibility(gradlePlugin(project)).getMinimumGradleVersion(), providerOf(GradleVersion.current().getVersion()));
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
            assertThat(subject.getDependencies().groovy(), providerOf(coordinate("org.codehaus.groovy:groovy-all:2.4.12")));
        }
    }
}
