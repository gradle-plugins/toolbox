package dev.gradleplugins;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
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

import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;


class GradlePluginDevelopmentDependenciesFunctionalTests {
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
            it.id("java-gradle-plugin");
        });
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
        public GradleBuildFile buildFile() {
            return buildFile;
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return settingsFile;
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
        public ExpressionBuilder<?> modifierDsl() {
            return literal("gradlePlugin.dependencies.platform");
        }

        @Override
        public GradleBuildFile buildFile() {
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
        public ExpressionBuilder<?> modifierDsl() {
            return literal("gradlePlugin.dependencies.enforcedPlatform");
        }

        @Override
        public GradleBuildFile buildFile() {
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
        public ExpressionBuilder<?> bucketDsl() {
            return literal("gradlePlugin.dependencies.api");
        }

        @Override
        public GradleBuildFile buildFile() {
            return buildFile;
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return settingsFile;
        }
    }

    @Nested
    class ImplementationDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public ExpressionBuilder<?> bucketDsl() {
            return literal("gradlePlugin.dependencies.implementation");
        }

        @Override
        public GradleBuildFile buildFile() {
            return buildFile;
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return settingsFile;
        }
    }

    @Nested
    class CompileOnlyDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public ExpressionBuilder<?> bucketDsl() {
            return literal("gradlePlugin.dependencies.compileOnly");
        }

        @Override
        public GradleBuildFile buildFile() {
            return buildFile;
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return settingsFile;
        }
    }

    @Nested
    class RuntimeOnlyDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public ExpressionBuilder<?> bucketDsl() {
            return literal("gradlePlugin.dependencies.runtimeOnly");
        }

        @Override
        public GradleBuildFile buildFile() {
            return buildFile;
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return settingsFile;
        }
    }

    @Nested
    class AnnotationProcessorDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return runner;
        }

        @Override
        public ExpressionBuilder<?> bucketDsl() {
            return literal("gradlePlugin.dependencies.annotationProcessor");
        }

        @Override
        public GradleBuildFile buildFile() {
            return buildFile;
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return settingsFile;
        }
    }
}
