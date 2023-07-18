package dev.gradleplugins;

import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin;
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class GroovyGradlePluginIncompatibilityFunctionalTests {
    @TempDir
    Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();

    @BeforeEach
    void givenProject() throws IOException {
        new GroovyBasicGradlePlugin().writeToProject(testDirectory.toFile());
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "buildscript {",
                "  dependencies {",
                "    classpath files(" + runner.getPluginClasspath().stream().map(File::toURI).map(it -> "'" + it + "'").collect(joining(",")) + ")",
                "  }",
                "}",
                "",
                "plugins {",
                "  id 'dev.gradleplugins.groovy-gradle-plugin'",
                "}",
                "",
                "tasks.register('verify')"
        ));

        runner = runner.inDirectory(testDirectory).withTasks("verify");
    }

    @Test
    void whenGroovyGradlePluginApplied_failsTheBuild() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
                "apply plugin: 'dev.gradleplugins.java-gradle-plugin'"
        ), StandardOpenOption.APPEND);

        assertThat(runner.buildAndFail().getOutput(),
                containsString("The 'dev.gradleplugins.java-gradle-plugin' cannot be applied with 'dev.gradleplugins.groovy-gradle-plugin', please apply just one of them."));
    }
}
