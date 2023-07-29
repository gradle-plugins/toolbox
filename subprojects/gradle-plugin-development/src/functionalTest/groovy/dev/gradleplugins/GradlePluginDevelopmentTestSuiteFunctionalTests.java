package dev.gradleplugins;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureCause;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureDescription;
import static org.hamcrest.MatcherAssert.assertThat;

class GradlePluginDevelopmentTestSuiteFunctionalTests {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath().inDirectory(() -> testDirectory);
    BuildScriptFile buildFile;

    @BeforeEach
    void givenProject() throws IOException {
        buildFile = new BuildScriptFile(testDirectory.resolve("build.gradle"));
        Files.write(testDirectory.resolve("settings.gradle"), Arrays.asList(
                "buildscript {",
                "  dependencies {",
                "    classpath files(" + runner.getPluginClasspath().stream().map(it -> "'" + it + "'").collect(Collectors.joining(", ")) + ")",
                "  }",
                "}",
                "rootProject.name = 'gradle-plugin'"
        ));
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "plugins {",
                "  id(\"dev.gradleplugins.gradle-plugin-testing-base\")",
                "  id(\"java-gradle-plugin\")",
                "}",
                "",
                "def testSuiteUnderTest = testSuiteFactory.create(\"foo\")"
        ));
    }

    @Test
    void hasMainSourceSetAsTestedSourceSetConvention() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "testSuiteUnderTest.testedSourceSet = null",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert testSuiteUnderTest.testedSourceSet.orNull?.name == 'main'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void usesDevelPluginSourceSetAsTestedSourceSetConvention() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "gradlePlugin.pluginSourceSet(sourceSets.create('anotherMain'))",
                "testSuiteUnderTest.testedSourceSet = null",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert testSuiteUnderTest.testedSourceSet.orNull?.name == 'anotherMain'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void disallowChangesToSourceSetProperty() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "afterEvaluate {",
                "  testSuiteUnderTest.sourceSet = null", // expect failure
                "}",
                "tasks.register('verify')"
        ), StandardOpenOption.APPEND);

        BuildResult result = runner.withTasks("verify").buildAndFail();
        assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
        assertThat(result, hasFailureCause("The value for property 'sourceSet' is final and cannot be changed any further."));
    }

    @Test
    void disallowChangesToTestedSourceSetProperty() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "afterEvaluate {",
                "  testSuiteUnderTest.testedSourceSet = null", // expect failure
                "}",
                "tasks.register('verify')"
        ), StandardOpenOption.APPEND);

        BuildResult result = runner.withTasks("verify").buildAndFail();
        assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
        assertThat(result, hasFailureCause("The value for property 'testedSourceSet' is final and cannot be changed any further."));
    }

    @Test
    void disallowChangesToTestingStrategiesProperty() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "afterEvaluate {",
                "  testSuiteUnderTest.testingStrategies = null", // expect failure
                "}",
                "tasks.register('verify')"
        ), StandardOpenOption.APPEND);

        BuildResult result = runner.withTasks("verify").buildAndFail();
        assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
        assertThat(result, hasFailureCause("The value for property 'testingStrategies' is final and cannot be changed any further."));
    }

    @Test
    void returnsTestTasksOnTaskViewElementQuery() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "testSuiteUnderTest.with {",
                "  testingStrategies = [strategies.coverageForGradleVersion('6.8'), strategies.coverageForGradleVersion('7.1')]",
                "}",
                "",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert testSuiteUnderTest.testTasks.elements.get().every { it.name == \"${testSuiteUnderTest.name}6.8\" || it.name == \"${testSuiteUnderTest.name}7.1\" }",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void includesPluginUnderTestMetadataConfigurationDependencies() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "testSuiteUnderTest.dependencies.pluginUnderTestMetadata files('my/own/dep.jar')",
                "",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert testSuiteUnderTest.pluginUnderTestMetadataTask.get().pluginClasspath.any { it.path.endsWith('/my/own/dep.jar') }",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void addsPluginUnderTestMetadataAsRuntimeOnlyDependency() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert testSuiteUnderTest.dependencies.runtimeOnly.asConfiguration.get().dependencies.any {",
                "      it instanceof SelfResolvingDependency && it.files.singleFile.path.endsWith('/pluginUnderTestMetadataFoo')",
                "    }",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void doesNotIncludesSourceSetInDevelTestSourceSets() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert !gradlePlugin.testSourceSets.any { it.name == 'foo' }",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void createsDefaultSourceSetOnSourceSetPropertyQueryOfConvention() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "testSuiteUnderTest.sourceSet = null // reset value to convention",
                "assert testSuiteUnderTest.sourceSet.get().name == 'foo'",
                "assert sourceSets.findByName('foo') != null",
                "",
                "tasks.register('verify')"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void createsDefaultSourceSetWhenProjectConfigured() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert sourceSets.findByName('foo') != null",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }


    @Test
    void doesNotCreateDefaultSourceSetWhenSourceSetPropertyOverridden() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "testSuiteUnderTest.sourceSet = sourceSets.create('kiel')",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert sourceSets.findByName('foo') == null",
                "    assert sourceSets.findByName('kiel') != null",
                "    assert testSuiteUnderTest.sourceSet.get().name == 'kiel'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void doesNotCreateDefaultSourceSetOnTestSuiteCreation() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "assert sourceSets.findByName('foo') == null : 'not yet created until sourceSet property realized'",
                "",
                "tasks.register('verify')"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Nested
    class DependenciesTest extends GradlePluginDevelopmentTestSuiteDependenciesTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public String testSuiteDsl() {
            return "testSuiteUnderTest";
        }

        @Override
        public BuildScriptFile buildFile() {
            return buildFile;
        }
    }
}
