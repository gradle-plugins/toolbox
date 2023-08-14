package dev.gradleplugins;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;

class GradlePluginDevelopmentBasePluginJvmCompatibilityOverrideFunctionalTests {
    @TempDir Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(() -> testDirectory).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();
    GradleBuildFile buildFile;

    @BeforeEach
    void givenProject() throws IOException {
        new JavaBasicGradlePlugin().writeToProject(testDirectory.toFile());
        buildFile = GradleBuildFile.inDirectory(testDirectory);
        buildFile.plugins(it -> it.id("dev.gradleplugins.gradle-plugin-base"));
        buildFile.append(groovyDsl("tasks.register('verify')"));

        runner = runner.withTasks("verify");
    }

    @Test
    void whenJavaPluginApplied_defaultToCurrentlyRunningGradle() {
        buildFile.append(groovyDsl(
            "apply plugin: 'java'",
            "",
            "tasks.named('verify') {",
            "  doLast {",
            "    assert java.targetCompatibility.toString() == '" + System.getProperty("java.specification.version") + "'",
            "    assert java.sourceCompatibility.toString() == '" + System.getProperty("java.specification.version") + "'",
            "  }",
            "}"
        ));

        runner.build();
    }

    @Test
    void whenJavaGradlePluginApplied_defaultToCurrentlyRunningGradle() throws IOException {
        buildFile.append(groovyDsl(
                "apply plugin: 'java-gradle-plugin'",
                "",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert java.targetCompatibility.toString() == '" + System.getProperty("java.specification.version") + "'",
                "    assert java.sourceCompatibility.toString() == '" + System.getProperty("java.specification.version") + "'",
                "  }",
                "}"
        ));

        runner.build();
    }

    @Test
    void whenJavaGradlePluginAppliedAndMinimumGradleVersionConfigured_doesNotChangeSourceCompatibilityIfOverridden() {
        Assumptions.assumeFalse(SystemUtils.IS_JAVA_11);

        buildFile.append(groovyDsl(
            "apply plugin: 'java-gradle-plugin'",
            "gradlePlugin.compatibility.minimumGradleVersion = '6.2.1'",
            "",
            "java.sourceCompatibility = JavaVersion.VERSION_11",
            "",
            "tasks.named('verify') {",
            "  doLast {",
            "    assert java.sourceCompatibility.toString() == '11'",
            "  }",
            "}"
        ));

        runner.build();
    }

    @Test
    void whenJavaGradlePluginAppliedAndMinimumGradleVersionConfigured_doesNotChangeTargetCompatibilityIfOverridden() {
        Assumptions.assumeFalse(SystemUtils.IS_JAVA_11);

        buildFile.append(groovyDsl(
                "apply plugin: 'java-gradle-plugin'",
                "gradlePlugin.compatibility.minimumGradleVersion = '6.2.1'",
                "",
                "java.targetCompatibility = JavaVersion.VERSION_11",
                "",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert java.targetCompatibility.toString() == '11'",
                "    assert java.sourceCompatibility.toString() == '" + System.getProperty("java.specification.version") + "'",
                "  }",
                "}"
        ));

        runner.build();
    }

    @Test
    void whenJavaGradlePluginApplied_doesNotChangeSourceCompatibilityIfOverridden() {
        Assumptions.assumeFalse(SystemUtils.IS_JAVA_11);

        buildFile.append(groovyDsl(
                "apply plugin: 'java-gradle-plugin'",
                "",
                "java.sourceCompatibility = JavaVersion.VERSION_11",
                "",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert java.sourceCompatibility.toString() == '11'",
                "  }",
                "}"
        ));

        runner.build();
    }

    @Test
    void whenJavaGradlePluginApplied_doesNotChangeTargetCompatibilityIfOverridden() {
        Assumptions.assumeFalse(SystemUtils.IS_JAVA_11);

        buildFile.append(groovyDsl(
                "apply plugin: 'java-gradle-plugin'",
                "",
                "java.targetCompatibility = JavaVersion.VERSION_11",
                "",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert java.targetCompatibility.toString() == '11'",
                "    assert java.sourceCompatibility.toString() == '" + System.getProperty("java.specification.version") + "'",
                "  }",
                "}"
        ));

        runner.build();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', useHeadersInDisplayName = true, value = {
            "gradleVersion  | minimumJavaVersion",
            "6.2            | 1.8",
            "5.1            | 1.8",
            "4.3            | 1.7",
            "3.0            | 1.7",
            "2.14           | 1.6",
            "1.12           | 1.5",
    })
    void whenJavaGradlePluginAppliedAndMinimumGradleVersion_defaultToMinimumJavaForThatGradleVersion(String gradleVersion, String javaVersion) {
        buildFile.append(groovyDsl(
                "apply plugin: 'java-gradle-plugin'",
                "",
                "gradlePlugin.compatibility.minimumGradleVersion = '" + gradleVersion + "'",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert java.targetCompatibility.toString() == '" + javaVersion + "'",
                "    assert java.sourceCompatibility.toString() == '" + javaVersion + "'",
                "  }",
                "}"
        ));

        runner.build();
    }
}
