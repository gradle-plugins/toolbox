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
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class ConfigurationCacheGradleApiVersionFunctionalTests {
    @TempDir
    Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();

    // TODO: Write a note regarding configuration-cache -> gradleApiVersion must be marked as used during configuration time because it impact the gradleAPI dependencies

    @BeforeEach
    void givenProject() throws IOException {
        new JavaBasicGradlePlugin().writeToProject(testDirectory.toFile());
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "plugins {",
                "  id 'dev.gradleplugins.gradle-plugin-base'",
                "  id 'java-gradle-plugin'",
                "}",
                "",
                "repositories {",
                "  gradlePluginDevelopment()",
                "}",
                "",
                "gradlePlugin.compatibility.gradleApiVersion = '6.4'"
        ));

        runner = runner.inDirectory(testDirectory).forwardOutput().withArguments("--configuration-cache").withTasks("build");
        assertThat(runner.build().getOutput(), containsString("Calculating task graph"));
        assertThat(runner.build().getOutput(), containsString("Reusing configuration cache."));
    }

    @Test
    void whenGradleApiVersionDoesNotChanges_reusesConfigurationCache() {
        assertThat(runner.build().getOutput(), containsString("Reusing configuration cache."));
    }
}
