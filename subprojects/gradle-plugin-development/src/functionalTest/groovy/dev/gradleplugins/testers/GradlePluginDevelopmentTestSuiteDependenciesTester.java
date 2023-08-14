package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static dev.gradleplugins.buildscript.GradleDsl.GROOVY;
import static dev.gradleplugins.buildscript.blocks.GradleBuildScriptBlocks.doLast;
import static dev.gradleplugins.buildscript.blocks.GradleBuildScriptBlocks.registerTask;
import static dev.gradleplugins.buildscript.syntax.Syntax.assertTrue;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public abstract class GradlePluginDevelopmentTestSuiteDependenciesTester {
    public abstract GradleRunner runner();

    public abstract ExpressionBuilder<?> testSuiteDsl();

    public abstract GradleBuildFile buildFile();
    public abstract GradleSettingsFile settingsFile();

    @Nested
    class GradleApiDependencyTest extends GradleApiDependencyTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public GradleBuildFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Override
        public String gradleApiDsl(String version) {
            return testSuiteDsl().toString(GROOVY) + ".dependencies.gradleApi('" + version + "')";
        }
    }

    @Nested
    class GradleTestKitDependencyTest extends GradleTestKitDependencyTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public GradleBuildFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Override
        public String gradleTestKitDsl(String version) {
            return testSuiteDsl().toString(GROOVY) + ".dependencies.gradleTestKit('" + version + "')";
        }

        @Override
        public String gradleTestKitDsl() {
            return testSuiteDsl().toString(GROOVY) + ".dependencies.gradleTestKit()";
        }
    }

    @Nested
    class ProjectDependencyTest extends ProjectDependencyTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public GradleBuildFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.settingsFile();
        }

        @Override
        public String projectDsl(String projectPath) {
            return testSuiteDsl().toString(GROOVY) + ".dependencies.project('" + projectPath + "')";
        }

        @Override
        public String projectDsl() {
            return testSuiteDsl().toString(GROOVY) + ".dependencies.project()";
        }
    }

    @Nested
    class PlatformDependencyModifierTest extends PlatformDependencyModifierTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public ExpressionBuilder<?> modifierDsl() {
            return testSuiteDsl().dot("dependencies.platform");
        }

        @Override
        public GradleBuildFile buildFile() {
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
        public ExpressionBuilder<?> modifierDsl() {
            return testSuiteDsl().dot("dependencies.enforcedPlatform");
        }

        @Override
        public GradleBuildFile buildFile() {
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
        public ExpressionBuilder<?> modifierDsl() {
            return testSuiteDsl().dot("dependencies.testFixtures");
        }

        @Override
        public GradleBuildFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.settingsFile();
        }
    }

    @Nested
    class ImplementationDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public ExpressionBuilder<?> bucketDsl() {
            return testSuiteDsl().dot("dependencies.implementation");
        }

        @Override
        public GradleBuildFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.settingsFile();
        }
    }

    @Nested
    class CompileOnlyDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public ExpressionBuilder<?> bucketDsl() {
            return testSuiteDsl().dot("dependencies.compileOnly");
        }

        @Override
        public GradleBuildFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.settingsFile();
        }
    }

    @Nested
    class RuntimeOnlyDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public ExpressionBuilder<?> bucketDsl() {
            return testSuiteDsl().dot("dependencies.runtimeOnly");
        }

        @Override
        public GradleBuildFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.settingsFile();
        }
    }

    @Nested
    class AnnotationProcessorDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public ExpressionBuilder<?> bucketDsl() {
            return testSuiteDsl().dot("dependencies.annotationProcessor");
        }

        @Override
        public GradleBuildFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.settingsFile();
        }
    }

    @Nested
    class PluginUnderTestMetadataDependencyBucketTest extends DependencyBucketTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        @Override
        public ExpressionBuilder<?> bucketDsl() {
            return testSuiteDsl().dot("dependencies.pluginUnderTestMetadata");
        }

        @Override
        public GradleBuildFile buildFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.buildFile();
        }

        @Override
        public GradleSettingsFile settingsFile() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.settingsFile();
        }

        @Test
        void isResolvableDependencyBucket() {
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(doLastBlock -> {
                    doLastBlock.add(assertTrue(bucketDsl().dot("asConfiguration.get().isCanBeConsumed()").negate()));
                    doLastBlock.add(assertTrue(bucketDsl().dot("asConfiguration.get().isCanBeResolved()")));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void hasDescription() {
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(doLastBlock -> {
                    doLastBlock.add(assertTrue(bucketDsl().dot("asConfiguration.get().description").equalTo(string("Plugin under test metadata for source set '").plus(testSuiteDsl().dot("name")).plus(string("'.")))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void hasJavaRuntimeUsageAttribute() throws IOException {
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(doLastBlock -> {
                    doLastBlock.add(assertTrue(bucketDsl().dot("asConfiguration.get().attributes.getAttribute(Usage.USAGE_ATTRIBUTE).name").equalTo(string("java-runtime"))));
                }));
            }));

            runner().withTasks("verify").build();
        }
    }
}
