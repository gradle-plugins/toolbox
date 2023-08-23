package dev.gradleplugins;

import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.testers.GradleApiDependencyTester;
import dev.gradleplugins.testers.GradlePluginDependencyTester;
import dev.gradleplugins.testers.GradleTestKitDependencyTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class ProjectDependenciesExtensionFunctionalTests {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS) Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath().inDirectory(() -> testDirectory);
    BuildScriptFile buildFile;

    @BeforeEach
    void setup() throws IOException {
        buildFile = new BuildScriptFile(testDirectory.resolve("build.gradle"));
        buildFile.append(
                "plugins {",
                "  id(\"dev.gradleplugins.gradle-plugin-base\")",
                "}"
        );
    }

    @Nested
    class GradleApiDependencyTest extends GradleApiDependencyTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public Path buildFile() {
            return testDirectory.resolve("build.gradle");
        }

        @Override
        public String gradleApiDsl(String version) {
            return "dependencies.gradleApi('" + version + "')";
        }
    }

    @Nested
    class GradleTestKitDependencyTest extends GradleTestKitDependencyTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public Path buildFile() {
            return testDirectory.resolve("build.gradle");
        }

        @Override
        public String gradleTestKitDsl(String version) {
            return "dependencies.gradleTestKit('" + version + "')";
        }

        @Override
        public String gradleTestKitDsl() {
            return "dependencies.gradleTestKit()";
        }
    }

    @Nested
    class GradlePluginDependencyTest extends GradlePluginDependencyTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public GradleBuildFile buildFile() {
            return buildFile;
        }

        @Override
        public Expression gradlePluginDsl(String pluginNotation) {
            return groovyDsl("dependencies.gradlePlugin('" + pluginNotation + "')");
        }
    }
}
