package dev.gradleplugins;

import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.runnerkit.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureCause;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureDescription;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GradlePluginDevelopmentBasePluginGradleApiVersionFunctionalTests {
    @TempDir
    Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();

    @BeforeEach
    void givenProject() throws IOException {
        new JavaBasicGradlePlugin().writeToProject(testDirectory.toFile());
        Files.write(testDirectory.resolve("settings.gradle"), Arrays.asList(
                "rootProject.name = 'gradle-plugin'"
        ));
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "plugins {",
                "  id 'dev.gradleplugins.gradle-plugin-base'",
                "  id 'java-gradle-plugin'",
                "}",
                "",
                "tasks.register('verify')"
        ));

        runner = runner.inDirectory(testDirectory).withTasks("verify");
    }

    @Test
    void whenProjectEvaluates_locksGradleApiVersionProperty() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "afterEvaluate {",
                "  gradlePlugin.compatibility.gradleApiVersion = '1.1.1'", // expect failure
                "}"
        ), StandardOpenOption.APPEND);

        BuildResult result = runner.buildAndFail();
        assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
        assertThat(result, hasFailureCause("The value for property 'gradleApiVersion' cannot be changed any further."));
    }

    @Test
    void defaultsToLocalGradleApi() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == 'local'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void doesNotOverrideSelectedGradleApiVersion() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "gradlePlugin.compatibility.gradleApiVersion = '6.2.1'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == '6.2.1'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenBothMinimumGradleVersionAndGradleApiVersionSelected_doesNotOverrideSelectedGradleApiVersion() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "gradlePlugin.compatibility.minimumGradleVersion = '6.2.1'",
                "gradlePlugin.compatibility.gradleApiVersion = '7.0'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == '7.0'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenMinimumGradleVersionIsGlobalAvailable_useMinimumGradleVersionAsGradleApiVersion() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "gradlePlugin.compatibility.minimumGradleVersion = '6.3'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == '6.3'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenMinimumGradleVersionIsReleaseCandidate_useLocalVersionAsGradleApiVersion() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "gradlePlugin.compatibility.minimumGradleVersion = '8.2-rc-2'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == 'local'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenMinimumGradleVersionIsMilestone_useLocalVersionAsGradleApiVersion() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "gradlePlugin.compatibility.minimumGradleVersion = '8.2-milestone-1'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == 'local'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }

    @Test
    void whenMinimumGradleVersionIsNightly_useLocalVersionAsGradleApiVersion() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "gradlePlugin.compatibility.minimumGradleVersion = '8.3-20230607222044+0000'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.gradleApiVersion.orNull == 'local'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        assertThat(runner.build().task(":verify").getOutcome(), equalTo(TaskOutcome.SUCCESS));
    }
}
