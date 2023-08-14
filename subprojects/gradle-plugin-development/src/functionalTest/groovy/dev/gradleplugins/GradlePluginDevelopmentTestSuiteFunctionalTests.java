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

import static dev.gradleplugins.buildscript.ast.expressions.AssignmentExpression.assign;
import static dev.gradleplugins.buildscript.ast.expressions.MethodCallExpression.call;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.blocks.BuildscriptBlock.classpath;
import static dev.gradleplugins.buildscript.blocks.DependencyNotation.files;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureCause;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureDescription;
import static org.hamcrest.MatcherAssert.assertThat;

class GradlePluginDevelopmentTestSuiteFunctionalTests {
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
            it.id("dev.gradleplugins.gradle-plugin-testing-base");
            it.id("java-gradle-plugin");
        });
        buildFile.append(val("testSuiteUnderTest", assign(call("testSuiteFactory.create", string("foo")))));
    }

    @Test
    void hasMainSourceSetAsTestedSourceSetConvention() {
        buildFile.append(groovyDsl(
                "testSuiteUnderTest.testedSourceSet = null",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert testSuiteUnderTest.testedSourceSet.orNull?.name == 'main'",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void usesDevelPluginSourceSetAsTestedSourceSetConvention() {
        buildFile.append(groovyDsl(
                "gradlePlugin.pluginSourceSet(sourceSets.create('anotherMain'))",
                "testSuiteUnderTest.testedSourceSet = null",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert testSuiteUnderTest.testedSourceSet.orNull?.name == 'anotherMain'",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void disallowChangesToSourceSetProperty() {
        buildFile.append(groovyDsl(
                "afterEvaluate {",
                "  testSuiteUnderTest.sourceSet = null", // expect failure
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
                "  testSuiteUnderTest.testedSourceSet = null", // expect failure
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
                "  testSuiteUnderTest.testingStrategies = null", // expect failure
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
                "testSuiteUnderTest.with {",
                "  testingStrategies = [strategies.coverageForGradleVersion('6.8'), strategies.coverageForGradleVersion('7.1')]",
                "}",
                "",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert testSuiteUnderTest.testTasks.elements.get().every { it.name == \"${testSuiteUnderTest.name}6.8\" || it.name == \"${testSuiteUnderTest.name}7.1\" }",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void includesPluginUnderTestMetadataConfigurationDependencies() {
        buildFile.append(groovyDsl(
                "testSuiteUnderTest.dependencies.pluginUnderTestMetadata files('my/own/dep.jar')",
                "",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert testSuiteUnderTest.pluginUnderTestMetadataTask.get().pluginClasspath.any { it.path.endsWith('/my/own/dep.jar') }",
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
                "    assert testSuiteUnderTest.dependencies.runtimeOnly.asConfiguration.get().dependencies.any {",
                "      it instanceof SelfResolvingDependency && it.files.singleFile.path.endsWith('/pluginUnderTestMetadataFoo')",
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
                "    assert !gradlePlugin.testSourceSets.any { it.name == 'foo' }",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void createsDefaultSourceSetOnSourceSetPropertyQueryOfConvention() {
        buildFile.append(groovyDsl(
                "testSuiteUnderTest.sourceSet = null // reset value to convention",
                "assert testSuiteUnderTest.sourceSet.get().name == 'foo'",
                "assert sourceSets.findByName('foo') != null",
                "",
                "tasks.register('verify')"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void createsDefaultSourceSetWhenProjectConfigured() {
        buildFile.append(groovyDsl(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert sourceSets.findByName('foo') != null",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }


    @Test
    void doesNotCreateDefaultSourceSetWhenSourceSetPropertyOverridden() {
        buildFile.append(groovyDsl(
                "testSuiteUnderTest.sourceSet = sourceSets.create('kiel')",
                "tasks.register('verify') {",
                "  doLast {",
                "    assert sourceSets.findByName('foo') == null",
                "    assert sourceSets.findByName('kiel') != null",
                "    assert testSuiteUnderTest.sourceSet.get().name == 'kiel'",
                "  }",
                "}"
        ));

        runner.withTasks("verify").build();
    }

    @Test
    void doesNotCreateDefaultSourceSetOnTestSuiteCreation() {
        buildFile.append(groovyDsl(
                "assert sourceSets.findByName('foo') == null : 'not yet created until sourceSet property realized'",
                "",
                "tasks.register('verify')"
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
            return literal("testSuiteUnderTest");
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
