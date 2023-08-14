package dev.gradleplugins;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class ConfigurationCacheMinimumGradleVersionFunctionalTests {
    @TempDir Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(() -> testDirectory).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();
    GradleBuildFile buildFile;

    // TODO: Write a note regarding configuration-cache -> minimumGradleVersion must be marked as used during configuration time because it impact the sourceCompatibility, targetCompatibility and gradleAPI dependencies (indirectly)

    @BeforeEach
    void givenProject() throws IOException {
        new JavaBasicGradlePlugin().writeToProject(testDirectory.toFile());
        buildFile = GradleBuildFile.inDirectory(testDirectory);
        buildFile.plugins(it -> {
            it.id("dev.gradleplugins.gradle-plugin-base");
            it.id("java-gradle-plugin");
        });
        buildFile.append(groovyDsl(
                "repositories {",
                "  gradlePluginDevelopment()",
                "}",
                "",
                "gradlePlugin.compatibility.minimumGradleVersion = '6.4'"
        ));

        runner = runner.forwardOutput().withArguments("--configuration-cache").withTasks("build");
        assertThat(runner.build().getOutput(), containsString("Calculating task graph"));
        assertThat(runner.build().getOutput(), containsString("Reusing configuration cache."));
    }

    @Test
    void whenMinimumGradleVersionDoesNotChanges_reusesConfigurationCache() {
        assertThat(runner.build().getOutput(), containsString("Reusing configuration cache."));
    }
}
