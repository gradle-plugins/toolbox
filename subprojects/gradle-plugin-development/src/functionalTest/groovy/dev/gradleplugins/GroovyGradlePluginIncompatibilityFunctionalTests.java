package dev.gradleplugins;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.ApplyStatement.Notation.plugin;
import static dev.gradleplugins.buildscript.blocks.ApplyStatement.apply;
import static dev.gradleplugins.buildscript.blocks.BuildscriptBlock.classpath;
import static dev.gradleplugins.buildscript.blocks.DependencyNotation.files;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class GroovyGradlePluginIncompatibilityFunctionalTests {
    @TempDir
    Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(() -> testDirectory).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();
    GradleBuildFile buildFile;

    @BeforeEach
    void givenProject() throws IOException {
        new GroovyBasicGradlePlugin().writeToProject(testDirectory.toFile());

        buildFile = GradleBuildFile.inDirectory(testDirectory);
        buildFile.buildscript(it -> it.dependencies(classpath(files(runner.getPluginClasspath()))));
        buildFile.plugins(it -> it.id("dev.gradleplugins.groovy-gradle-plugin"));
        buildFile.append(groovyDsl("tasks.register('verify')"));

        runner = runner.inDirectory(testDirectory).withTasks("verify");
    }

    @Test
    void whenGroovyGradlePluginApplied_failsTheBuild() {
        buildFile.append(apply(plugin("dev.gradleplugins.java-gradle-plugin")));

        assertThat(runner.buildAndFail().getOutput(),
                containsString("The 'dev.gradleplugins.java-gradle-plugin' cannot be applied with 'dev.gradleplugins.groovy-gradle-plugin', please apply just one of them."));
    }
}
