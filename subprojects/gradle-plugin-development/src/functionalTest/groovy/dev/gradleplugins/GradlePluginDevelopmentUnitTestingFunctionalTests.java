package dev.gradleplugins;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.testers.GradlePluginDevelopmentTestSuiteDependenciesTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.BuildscriptBlock.classpath;
import static dev.gradleplugins.buildscript.blocks.DependencyNotation.files;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureCause;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureDescription;
import static org.hamcrest.MatcherAssert.assertThat;

class GradlePluginDevelopmentUnitTestingFunctionalTests {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath().inDirectory(() -> testDirectory);
    GradleBuildFile buildFile;
    GradleSettingsFile settingsFile;

    @BeforeEach
    void givenProject() throws IOException {
        settingsFile = GradleSettingsFile.inDirectory(testDirectory);
        settingsFile.buildscript(it -> it.dependencies(classpath(files(runner.getPluginClasspath()))));
        settingsFile.append(groovyDsl("rootProject.name = 'gradle-plugin'"));

        buildFile = GradleBuildFile.inDirectory(testDirectory);
        buildFile.plugins(it -> it.id("dev.gradleplugins.gradle-plugin-unit-test"));
    }

    @Test // https://github.com/gradle-plugins/toolbox/issues/65
    void canAddDependenciesBeforeCoreGradleDevelPluginApplied() {
        // We need to avoid an implementation that rely on the sourceSet for the component dependencies.
        // If we do, the sourceSet can realize/register before we apply core Gradle plugins.
        // Those plugins assume the sourceSet does not exist which result in a failure.
        buildFile.append(groovyDsl(
                "test.dependencies.implementation 'org.junit.jupiter:junit-jupiter:5.8.1'",
                "apply plugin: 'java-gradle-plugin'",
                "",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert configurations.testImplementation.dependencies.any { 'org.junit.jupiter:junit-jupiter:5.8.1' == \"${it.group}:${it.name}:${it.version}\" }",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Nested
    class GivenCorePluginAppliedTest {
        @BeforeEach
        void givenProject() throws IOException {
            buildFile.plugins(it -> it.id("java-gradle-plugin"));
        }

        @Test
        void hasMainSourceSetAsTestedSourceSetConvention() {
            buildFile.append(groovyDsl(
                    "test.testedSourceSet = null",
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert test.testedSourceSet.orNull?.name == 'main'",
                    "  }",
                    "}"
            ));

            runner.withTasks("verify").build();
        }

        @Test
        void usesDevelPluginSourceSetAsTestedSourceSetConvention() {
            buildFile.append(groovyDsl(
                    "gradlePlugin.pluginSourceSet(sourceSets.create('anotherMain'))",
                    "test.testedSourceSet = null",
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert test.testedSourceSet.orNull?.name == 'anotherMain'",
                    "  }",
                    "}"
            ));

            runner.withTasks("verify").build();
        }

        @Test
        void disallowChangesToSourceSetProperty() {
            buildFile.append(groovyDsl(
                    "afterEvaluate {",
                    "  test.sourceSet = null", // expect failure
                    "}",
                    "tasks.register('verify')"
            ));

            BuildResult result = runner.withTasks("verify").buildAndFail();
            assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
            assertThat(result, hasFailureCause("The value for property 'sourceSet' is final and cannot be changed any further."));
        }

        @Test
        void disallowChangesToTestedSourceSetProperty() {
            buildFile.append(groovyDsl(
                    "afterEvaluate {",
                    "  test.testedSourceSet = null", // expect failure
                    "}",
                    "tasks.register('verify')"
            ));

            BuildResult result = runner.withTasks("verify").buildAndFail();
            assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
            assertThat(result, hasFailureCause("The value for property 'testedSourceSet' is final and cannot be changed any further."));
        }

        @Test
        void disallowChangesToTestingStrategiesProperty() {
            buildFile.append(groovyDsl(
                    "afterEvaluate {",
                    "  test.testingStrategies = null", // expect failure
                    "}",
                    "tasks.register('verify')"
            ));

            BuildResult result = runner.withTasks("verify").buildAndFail();
            assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
            assertThat(result, hasFailureCause("The value for property 'testingStrategies' is final and cannot be changed any further."));
        }

        @Test
        void returnsTestTasksOnTaskViewElementQuery() {
            buildFile.append(groovyDsl(
                    "test {",
                    "  testingStrategies = [strategies.coverageForGradleVersion('6.8'), strategies.coverageForGradleVersion('7.1')]",
                    "}",
                    "",
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert test.testTasks.elements.get().every { it.name == 'test6.8' || it.name == 'test7.1' }",
                    "  }",
                    "}"
            ));

            runner.withTasks("verify").build();
        }

        @Test
        void includesPluginUnderTestMetadataConfigurationDependencies() {
            buildFile.append(groovyDsl(
                    "test.dependencies.pluginUnderTestMetadata files('my/own/dep.jar')",
                    "",
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert test.pluginUnderTestMetadataTask.get().pluginClasspath.any { it.path.endsWith('/my/own/dep.jar') }",
                    "  }",
                    "}"
            ));

            runner.withTasks("verify").build();
        }

        @Test
        void addsPluginUnderTestMetadataAsRuntimeOnlyDependency() {
            buildFile.append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert test.dependencies.runtimeOnly.asConfiguration.get().dependencies.any {",
                    "      it instanceof SelfResolvingDependency && it.files.singleFile.path.endsWith('/pluginUnderTestMetadataTest')",
                    "    }",
                    "  }",
                    "}"
            ));

            runner.withTasks("verify").build();
        }

        @Test
        void doesNotIncludesSourceSetInDevelTestSourceSets() {
            buildFile.append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert !gradlePlugin.testSourceSets.any { it.name == 'test' }",
                    "  }",
                    "}"
            ));

            runner.withTasks("verify").build();
        }

        @Test
        void hasGradleApiImplementationDependency() {
            buildFile.append(groovyDsl(
                    "apply plugin: 'dev.gradleplugins.gradle-plugin-base'",
                    "gradlePlugin.compatibility.minimumGradleVersion = '5.6'",
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert configurations.testImplementation.dependencies.any { 'dev.gradleplugins:gradle-api:5.6' == \"${it.group}:${it.name}:${it.version}\" }",
                    "  }",
                    "}"
            ));

            runner.withTasks("verify").build();
        }

        @Nested
        class DependenciesTest extends GradlePluginDevelopmentTestSuiteDependenciesTester {
            @Override
            public GradleRunner runner() {
                return runner;
            }

            @Override
            public ExpressionBuilder<?> testSuiteDsl() {
                return literal("test");
            }

            @Override
            public GradleBuildFile buildFile() {
                return buildFile;
            }

            @Override
            public GradleSettingsFile settingsFile() {
                return settingsFile;
            }
        }
    }
}
