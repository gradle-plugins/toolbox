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

class GradlePluginDevelopmentFunctionalTestingFunctionalTests {
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
                "  id(\"dev.gradleplugins.gradle-plugin-functional-test\")",
                "  id(\"java-gradle-plugin\")",
                "}"
        ));
    }

    @Test
    void hasMainSourceSetAsTestedSourceSetConvention() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "functionalTest.testedSourceSet = null",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert functionalTest.testedSourceSet.orNull?.name == 'main'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void usesDevelPluginSourceSetAsTestedSourceSetConvention() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "gradlePlugin.pluginSourceSet(sourceSets.create('anotherMain'))",
                "functionalTest.testedSourceSet = null",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert functionalTest.testedSourceSet.orNull?.name == 'anotherMain'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void disallowChangesToSourceSetProperty() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "afterEvaluate {",
                "  functionalTest.sourceSet = null", // expect failure
                "}",
                "tasks.register('verify')"
        ), StandardOpenOption.APPEND);

        BuildResult result = runner.withTasks("verify").buildAndFail();
        assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
        assertThat(result, hasFailureCause("The value for property 'sourceSet' is final and cannot be changed any further."));
    }

    @Test
    void addsPluginUnderTestMetadataAsRuntimeOnlyDependency() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert functionalTest.dependencies.runtimeOnly.asConfiguration.get().dependencies.any {",
                "      it instanceof SelfResolvingDependency && it.files.singleFile.path.endsWith('/pluginUnderTestMetadataFunctionalTest')",
                "    }",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void includesPluginUnderTestMetadataConfigurationDependencies() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "functionalTest.dependencies.pluginUnderTestMetadata files('my/own/dep.jar')",
                "",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert functionalTest.pluginUnderTestMetadataTask.get().pluginClasspath.any { it.path.endsWith('/my/own/dep.jar') }",
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
                "    assert !gradlePlugin.testSourceSets.any { it.name == 'functionalTest' }",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void hasGradleTestKitImplementationDependencyToLocalVersion() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert configurations.functionalTestImplementation.dependencies.any { it instanceof SelfResolvingDependency && it.targetComponentId?.displayName == 'Gradle TestKit' }",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.withTasks("verify").build();
    }

    @Test
    void hasGradleTestKitImplementationDependencyToGradleApiVersion() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "apply plugin: 'dev.gradleplugins.gradle-plugin-base'",
                "gradlePlugin.compatibility.gradleApiVersion = '5.6'",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert configurations.functionalTestImplementation.dependencies.any { 'dev.gradleplugins:gradle-test-kit:5.6' == \"${it.group}:${it.name}:${it.version}\" }",
                "  }",
                "}"
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
            return "functionalTest";
        }

        @Override
        public BuildScriptFile buildFile() {
            return buildFile;
        }
    }
}
