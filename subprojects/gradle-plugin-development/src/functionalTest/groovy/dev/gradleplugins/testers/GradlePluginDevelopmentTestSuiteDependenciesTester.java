package dev.gradleplugins.testers;

import dev.gradleplugins.BuildScriptFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public abstract class GradlePluginDevelopmentTestSuiteDependenciesTester {
    public abstract GradleRunner runner();

    public abstract String testSuiteDsl();

    public abstract BuildScriptFile buildFile();

    @Nested
    class GradleApiDependencyTest extends GradleApiDependencyTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public Path buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile().getLocation();
        }

        @Override
        public String gradleApiDsl(String version) {
            return testSuiteDsl() + ".dependencies.gradleApi('" + version + "')";
        }
    }

    @Nested
    class GradleTestKitDependencyTest extends GradleTestKitDependencyTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public Path buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile().getLocation();
        }

        @Override
        public String gradleTestKitDsl(String version) {
            return testSuiteDsl() + ".dependencies.gradleTestKit('" + version + "')";
        }

        @Override
        public String gradleTestKitDsl() {
            return testSuiteDsl() + ".dependencies.gradleTestKit()";
        }
    }

    @Nested
    class ProjectDependencyTest extends ProjectDependencyTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public Path buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile().getLocation();
        }

        @Override
        public String projectDsl(String projectPath) {
            return testSuiteDsl() + ".dependencies.project('" + projectPath + "')";
        }

        @Override
        public String projectDsl() {
            return testSuiteDsl() + ".dependencies.project()";
        }
    }

    @Nested
    class PlatformDependencyModifierTest extends PlatformDependencyModifierTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public String modifierDsl() {
            return testSuiteDsl() + ".dependencies.platform";
        }

        @Override
        public BuildScriptFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }
    }

    @Nested
    class EnforcedPlatformDependencyModifierTest extends EnforcedPlatformDependencyModifierTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public String modifierDsl() {
            return testSuiteDsl() + ".dependencies.enforcedPlatform";
        }

        @Override
        public BuildScriptFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }
    }

    @Nested
    class TestFixturesDependencyModifierTest extends TestFixturesDependencyModifierTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public String modifierDsl() {
            return testSuiteDsl() + ".dependencies.testFixtures";
        }

        @Override
        public BuildScriptFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }
    }

    @Nested
    class ImplementationDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public String bucketDsl() {
            return testSuiteDsl() + ".dependencies.implementation";
        }

        @Override
        public BuildScriptFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }
    }

    @Nested
    class CompileOnlyDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public String bucketDsl() {
            return testSuiteDsl() + ".dependencies.compileOnly";
        }

        @Override
        public BuildScriptFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }
    }

    @Nested
    class RuntimeOnlyDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public String bucketDsl() {
            return testSuiteDsl() + ".dependencies.runtimeOnly";
        }

        @Override
        public BuildScriptFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }
    }

    @Nested
    class AnnotationProcessorDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public String bucketDsl() {
            return testSuiteDsl() + ".dependencies.annotationProcessor";
        }

        @Override
        public BuildScriptFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }
    }

    @Nested
    class PluginUnderTestMetadataDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public String bucketDsl() {
            return testSuiteDsl() + ".dependencies.pluginUnderTestMetadata";
        }

        @Override
        public BuildScriptFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Test
        void isResolvableDependencyBucket() throws IOException {
            buildFile().append(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert !" + bucketDsl() + ".asConfiguration.get().canBeConsumed",
                    "    assert " + bucketDsl() + ".asConfiguration.get().canBeResolved",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void hasDescription() throws IOException {
            buildFile().append(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert " + bucketDsl() + ".asConfiguration.get().description == \"Plugin under test metadata for source set '${" + testSuiteDsl() + ".name}'.\"",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void hasJavaRuntimeUsageAttribute() throws IOException {
            buildFile().append(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert " + bucketDsl() + ".asConfiguration.get().attributes.getAttribute(Usage.USAGE_ATTRIBUTE).name == 'java-runtime'",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }
    }
}
