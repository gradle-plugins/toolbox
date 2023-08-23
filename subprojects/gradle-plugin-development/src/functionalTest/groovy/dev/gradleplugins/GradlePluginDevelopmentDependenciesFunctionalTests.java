package dev.gradleplugins;

import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.testers.DependencyBucketTester;
import dev.gradleplugins.testers.EnforcedPlatformDependencyModifierTester;
import dev.gradleplugins.testers.GradleApiDependencyTester;
import dev.gradleplugins.testers.GradlePluginDependencyTester;
import dev.gradleplugins.testers.PlatformDependencyModifierTester;
import dev.gradleplugins.testers.ProjectDependencyTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class GradlePluginDevelopmentDependenciesFunctionalTests {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS) Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath().inDirectory(() -> testDirectory);
    BuildScriptFile buildFile;

    @BeforeEach
    void setup() throws IOException {
        buildFile = new BuildScriptFile(testDirectory.resolve("build.gradle"));
        buildFile.append(
                "plugins {",
                "  id(\"dev.gradleplugins.gradle-plugin-base\")",
                "  id(\"java-gradle-plugin\")",
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
            return "gradlePlugin.dependencies.gradleApi('" + version + "')";
        }
    }

    @Nested
    class ProjectDependencyTest extends ProjectDependencyTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public Path buildFile() {
            return testDirectory.resolve("build.gradle");
        }

        @Override
        public String projectDsl(String projectPath) {
            return "gradlePlugin.dependencies.project('" + projectPath + "')";
        }

        @Override
        public String projectDsl() {
            return "gradlePlugin.dependencies.project()";
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
            return groovyDsl("gradlePlugin.dependencies.gradlePlugin('" + pluginNotation + "')");
        }
    }

    @Nested
    class PlatformDependencyModifierTest extends PlatformDependencyModifierTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public String modifierDsl() {
            return "gradlePlugin.dependencies.platform";
        }

        @Override
        public BuildScriptFile buildFile() {
            return buildFile;
        }
    }

    @Nested
    class EnforcedPlatformDependencyModifierTest extends EnforcedPlatformDependencyModifierTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public String modifierDsl() {
            return "gradlePlugin.dependencies.enforcedPlatform";
        }

        @Override
        public BuildScriptFile buildFile() {
            return buildFile;
        }
    }

    @Nested
    class ApiDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public String bucketDsl() {
            return "gradlePlugin.dependencies.api";
        }

        @Override
        public BuildScriptFile buildFile() {
            return buildFile;
        }
    }

    @Nested
    class ImplementationDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public String bucketDsl() {
            return "gradlePlugin.dependencies.implementation";
        }

        @Override
        public BuildScriptFile buildFile() {
            return buildFile;
        }
    }

    @Nested
    class CompileOnlyDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public String bucketDsl() {
            return "gradlePlugin.dependencies.compileOnly";
        }

        @Override
        public BuildScriptFile buildFile() {
            return buildFile;
        }
    }

    @Nested
    class RuntimeOnlyDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public String bucketDsl() {
            return "gradlePlugin.dependencies.runtimeOnly";
        }

        @Override
        public BuildScriptFile buildFile() {
            return buildFile;
        }
    }

    @Nested
    class AnnotationProcessorDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public String bucketDsl() {
            return "gradlePlugin.dependencies.annotationProcessor";
        }

        @Override
        public BuildScriptFile buildFile() {
            return buildFile;
        }
    }
}
