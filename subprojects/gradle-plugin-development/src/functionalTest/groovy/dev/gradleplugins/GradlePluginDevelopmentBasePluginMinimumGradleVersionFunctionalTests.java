package dev.gradleplugins;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureCause;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureDescription;
import static org.hamcrest.MatcherAssert.assertThat;

class GradlePluginDevelopmentBasePluginMinimumGradleVersionFunctionalTests {
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
    void whenProjectEvaluates_locksMinimumGradleVersionProperty() {
        buildFile.append(groovyDsl(
                "afterEvaluate {",
                "  gradlePlugin.compatibility.minimumGradleVersion = '1.1.1'", // expect failure
                "}"
        ));

        BuildResult result = runner.buildAndFail();
        assertThat(result, hasFailureDescription("A problem occurred configuring root project 'gradle-plugin'."));
        assertThat(result, hasFailureCause("The value for property 'minimumGradleVersion' is final and cannot be changed any further."));
    }

    @Test
    void doesNotHaveMinimumGradleVersionSpecifiedByDefault() {
        buildFile.append(groovyDsl(
                "tasks.named('verify') {",
                "  doLast {",
                "    assert !gradlePlugin.compatibility.minimumGradleVersion.present",
                "  }",
                "}"
        ));

        runner.build();
    }

    @Test
    void doesNotOverwriteSelectedMinimumGradleVersion() throws IOException {
        buildFile.append(groovyDsl(
                "gradlePlugin.compatibility.minimumGradleVersion = '6.2.1'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert gradlePlugin.compatibility.minimumGradleVersion.orNull == '6.2.1'",
                "  }",
                "}"
        ));

        runner.build();
    }
}
