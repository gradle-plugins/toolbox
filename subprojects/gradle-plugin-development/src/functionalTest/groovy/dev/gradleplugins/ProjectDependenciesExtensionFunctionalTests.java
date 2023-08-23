package dev.gradleplugins;

import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.testers.GradleApiDependencyTester;
import dev.gradleplugins.testers.GradlePluginDependencyTester;
import dev.gradleplugins.testers.GradleTestKitDependencyTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;

class ProjectDependenciesExtensionFunctionalTests {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS) Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath().inDirectory(() -> testDirectory);
    GradleBuildFile buildFile;

    @BeforeEach
    void setup() {
        buildFile = GradleBuildFile.inDirectory(testDirectory);
        buildFile.plugins(it -> it.id("dev.gradleplugins.gradle-plugin-base"));
    }

    @Nested
    class GradleApiDependencyTest extends GradleApiDependencyTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public GradleBuildFile buildFile() {
            return buildFile;
        }

        @Override
        public Expression gradleApiDsl(String version) {
            return groovyDsl("dependencies.gradleApi('" + version + "')");
        }
    }

    @Nested
    class GradleTestKitDependencyTest extends GradleTestKitDependencyTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public GradleBuildFile buildFile() {
            return buildFile;
        }

        @Override
        public Expression gradleTestKitDsl(String version) {
            return groovyDsl("dependencies.gradleTestKit('" + version + "')");
        }

        @Override
        public Expression gradleTestKitDsl() {
            return groovyDsl("dependencies.gradleTestKit()");
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
