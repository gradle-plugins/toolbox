package dev.gradleplugins;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;


class GradlePluginDevelopmentGeneratedGradleJarsFunctionalTests {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS) Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath().inDirectory(() -> testDirectory);
    GradleBuildFile buildFile;
    GradleSettingsFile settingsFile;

    @BeforeEach
    void setup() {
        settingsFile = GradleSettingsFile.inDirectory(testDirectory);
        buildFile = GradleBuildFile.inDirectory(testDirectory);
        buildFile.plugins(it -> {
            it.id("dev.gradleplugins.gradle-plugin-base");
            it.id("java-library");
        });

        buildFile.append(groovyDsl(
                "Set<String> allDependencies(Configuration configuration) {",
                "  return configuration.incoming.resolutionResult.allDependencies.collect { result ->",
                "    def it = result.requested",
                "    if (it instanceof ModuleComponentSelector) {",
                "      return \"${it.group}:${it.module}:${it.version}\".toString()",
                "    } else if (it instanceof ProjectComponentSelector) {",
                "      return it.projectPath",
                "    } else {",
                "      throw new RuntimeException()",
                "    }",
                "  }",
                "}"
        ));
    }

    @Nested
    class GradleApiDependencyTest {
        @Test
        void test() {
            buildFile.append(groovyDsl(
                    "repositories {",
                    "  gradleDistributions()",
                    "  mavenCentral()",
                    "}",
                    "dependencies {",
                    "  implementation gradleApi('8.9-rc-1')",
                    "}",
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert allDependencies(configurations.compileClasspath)",
                    "        .containsAll(['dev.gradleplugins.generated:gradle-api:8.9-rc-1', 'org.codehaus.groovy:groovy:3.0.21'])",
                    "    assert allDependencies(configurations.runtimeClasspath)",
                    "        .containsAll(['dev.gradleplugins.generated:gradle-api:8.9-rc-1', 'org.codehaus.groovy:groovy-all:3.0.21', 'org.jetbrains.kotlin:kotlin-stdlib:1.9.23'])",
                    "  }",
                    "}"
            ));

            runner.withArgument("verify").build();
        }
    }

    @Nested
    class GradleTestKitDependencyTest {
        @Test
        void test() {
            buildFile.append(groovyDsl(
                    "repositories {",
                    "  gradleDistributions()",
                    "  mavenCentral()",
                    "}",
                    "dependencies {",
                    "  implementation gradleTestKit('8.9-rc-1')",
                    "}",
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert allDependencies(configurations.compileClasspath)",
                    "        .containsAll(['dev.gradleplugins.generated:gradle-test-kit:8.9-rc-1', 'org.codehaus.groovy:groovy:3.0.21'])",
                    "    assert allDependencies(configurations.runtimeClasspath)",
                    "        .containsAll(['dev.gradleplugins.generated:gradle-test-kit:8.9-rc-1', 'org.codehaus.groovy:groovy-all:3.0.21', 'org.jetbrains.kotlin:kotlin-stdlib:1.9.23'])",
                    "  }",
                    "}"
            ));

            runner.withArgument("verify").build();
        }
    }
}
