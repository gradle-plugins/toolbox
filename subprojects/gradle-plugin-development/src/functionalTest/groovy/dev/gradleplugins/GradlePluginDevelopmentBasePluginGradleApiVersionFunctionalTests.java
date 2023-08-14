package dev.gradleplugins;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.runnerkit.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureCause;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureDescription;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GradlePluginDevelopmentBasePluginGradleApiVersionFunctionalTests {
    @TempDir
    Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(() -> testDirectory).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();
    GradleBuildFile buildFile;
    GradleSettingsFile settingsFile;

    @BeforeEach
    void givenProject() throws IOException {
        new JavaBasicGradlePlugin().writeToProject(testDirectory.toFile());
        settingsFile = GradleSettingsFile.inDirectory(testDirectory);
        settingsFile.append(groovyDsl("rootProject.name = 'gradle-plugin'"));

        buildFile = GradleBuildFile.inDirectory(testDirectory);
        buildFile.plugins(it -> {
            it.id("dev.gradleplugins.gradle-plugin-base");
            it.id("java-gradle-plugin");
        });
        buildFile.append(groovyDsl("tasks.register('verify')"));

        runner = runner.withTasks("verify");
    }

    @Test
    void whenProjectEvaluates_locksGradleApiVersionProperty() {
        buildFile.append(groovyDsl(
                "afterEvaluate {",
                "  gradlePlugin.compatibility.gradleApiVersion = '1.1.1'", // expect failure
                "}"
        ));

        BuildResult result = runner.buildAndFail();
        assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
        assertThat(result, hasFailureCause("The value for property 'gradleApiVersion' cannot be changed any further."));
    }

    @Test
    void defaultsToLocalGradleApi() {
        buildFile.append(groovyDsl(
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == 'local'",
                "  }",
                "}"
        ));

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void doesNotOverrideSelectedGradleApiVersion() {
        buildFile.append(groovyDsl(
                "gradlePlugin.compatibility.gradleApiVersion = '6.2.1'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == '6.2.1'",
                "  }",
                "}"
        ));

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenBothMinimumGradleVersionAndGradleApiVersionSelected_doesNotOverrideSelectedGradleApiVersion() {
        buildFile.append(groovyDsl(
                "gradlePlugin.compatibility.minimumGradleVersion = '6.2.1'",
                "gradlePlugin.compatibility.gradleApiVersion = '7.0'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == '7.0'",
                "  }",
                "}"
        ));

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenMinimumGradleVersionIsGlobalAvailable_useMinimumGradleVersionAsGradleApiVersion() throws IOException {
        buildFile.append(groovyDsl(
                "gradlePlugin.compatibility.minimumGradleVersion = '6.3'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == '6.3'",
                "  }",
                "}"
        ));

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenMinimumGradleVersionIsReleaseCandidate_useLocalVersionAsGradleApiVersion() {
        buildFile.append(groovyDsl(
                "gradlePlugin.compatibility.minimumGradleVersion = '8.2-rc-2'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == 'local'",
                "  }",
                "}"
        ));

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenMinimumGradleVersionIsMilestone_useLocalVersionAsGradleApiVersion() {
        buildFile.append(groovyDsl(
                "gradlePlugin.compatibility.minimumGradleVersion = '8.2-milestone-1'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == 'local'",
                "  }",
                "}"
        ));

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenMinimumGradleVersionIsNightly_useLocalVersionAsGradleApiVersion() {
        buildFile.append(groovyDsl(
                "gradlePlugin.compatibility.minimumGradleVersion = '8.3-20230607222044+0000'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == 'local'",
                "  }",
                "}"
        ));

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }
}
