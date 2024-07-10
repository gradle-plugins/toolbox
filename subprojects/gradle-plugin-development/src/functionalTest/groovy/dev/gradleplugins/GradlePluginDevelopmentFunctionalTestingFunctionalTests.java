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

class GradlePluginDevelopmentFunctionalTestingFunctionalTests {
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
        buildFile.plugins(it -> {
            it.id("dev.gradleplugins.gradle-plugin-functional-test");
            it.id("java-gradle-plugin");
        });
    }

    @Test
    void hasMainSourceSetAsTestedSourceSetConvention() {
        buildFile.append(groovyDsl(
                "functionalTest.testedSourceSet = null",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert functionalTest.testedSourceSet.orNull?.name == 'main'",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void usesDevelPluginSourceSetAsTestedSourceSetConvention() {
        buildFile.append(groovyDsl(
                "gradlePlugin.pluginSourceSet(sourceSets.create('anotherMain'))",
                "functionalTest.testedSourceSet = null",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert functionalTest.testedSourceSet.orNull?.name == 'anotherMain'",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void disallowChangesToSourceSetProperty() {
        buildFile.append(groovyDsl(
                "afterEvaluate {",
                "  functionalTest.sourceSet = null", // expect failure
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
                "  functionalTest.testedSourceSet = null", // expect failure
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
                "  functionalTest.testingStrategies = null", // expect failure
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
                "functionalTest {",
                "  testingStrategies = [strategies.coverageForGradleVersion('6.8'), strategies.coverageForGradleVersion('7.1')]",
                "}",
                "",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert functionalTest.testTasks.elements.get().every { it.name == \"${functionalTest.name}6.8\" || it.name == \"${functionalTest.name}7.1\" }",
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
                "    assert functionalTest.dependencies.runtimeOnly.asConfiguration.get().dependencies.any {",
                "      it instanceof SelfResolvingDependency && it.files.singleFile.path.endsWith('/pluginUnderTestMetadataFunctionalTest')",
                "    }",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void includesPluginUnderTestMetadataConfigurationDependencies() {
        buildFile.append(groovyDsl(
                "functionalTest.dependencies.pluginUnderTestMetadata files('my/own/dep.jar')",
                "",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert functionalTest.pluginUnderTestMetadataTask.get().pluginClasspath.any { it.path.endsWith('/my/own/dep.jar') }",
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
                "    assert !gradlePlugin.testSourceSets.any { it.name == 'functionalTest' }",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void hasGradleTestKitImplementationDependencyToLocalVersion() {
        buildFile.append(groovyDsl(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert configurations.functionalTestImplementation.dependencies.any { it instanceof SelfResolvingDependency && it.targetComponentId?.displayName == 'Gradle TestKit' }",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void hasGradleTestKitImplementationDependencyToGradleApiVersion() {
        buildFile.append(groovyDsl(
                "apply plugin: 'dev.gradleplugins.gradle-plugin-base'",
                "gradlePlugin.compatibility.gradleApiVersion = '5.6'",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert configurations.functionalTestImplementation.dependencies.any { 'dev.gradleplugins:gradle-test-kit:5.6' == \"${it.group}:${it.name}:${it.version}\" }",
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
            return literal("functionalTest");
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
