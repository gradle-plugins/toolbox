package dev.gradleplugins;

import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
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

class GradlePluginDevelopmentBasePluginMinimumGradleVersionFunctionalTests {
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
    void whenProjectEvaluates_locksMinimumGradleVersionProperty() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "afterEvaluate {",
                "  gradlePlugin.compatibility.minimumGradleVersion = '1.1.1'", // expect failure
                "}"
        ), StandardOpenOption.APPEND);

        BuildResult result = runner.buildAndFail();
        assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
        assertThat(result, hasFailureCause("The value for property 'minimumGradleVersion' is final and cannot be changed any further."));
    }

    @Test
    void doesNotHaveMinimumGradleVersionSpecifiedByDefault() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "tasks.named('verify') {",
                "  doLast {",
                "    assert !gradlePlugin.compatibility.minimumGradleVersion.present",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.build();
    }

    @Test
    void doesNotOverwriteSelectedMinimumGradleVersion() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "gradlePlugin.compatibility.minimumGradleVersion = '6.2.1'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.minimumGradleVersion.orNull == '6.2.1'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner.build();
    }
}
