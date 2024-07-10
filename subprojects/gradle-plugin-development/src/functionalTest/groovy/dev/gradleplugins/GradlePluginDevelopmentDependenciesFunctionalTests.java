package dev.gradleplugins;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.testers.DependencyBucketTester;
import dev.gradleplugins.testers.DependencyWiringTester;
import dev.gradleplugins.testers.EnforcedPlatformDependencyModifierTester;
import dev.gradleplugins.testers.GradleApiDependencyTester;
import dev.gradleplugins.testers.GradlePluginDependencyTester;
import dev.gradleplugins.testers.PlatformDependencyModifierTester;
import dev.gradleplugins.testers.ProjectDependencyTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.not;


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
        public Expression gradleApiDsl(String version) {
            return groovyDsl("gradlePlugin.dependencies.gradleApi('" + version + "')");
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
        public Expression projectDsl(String projectPath) {
            return groovyDsl("gradlePlugin.dependencies.project('" + projectPath + "')");
        }

        @Override
        public Expression projectDsl() {
            return groovyDsl("gradlePlugin.dependencies.project()");
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


    abstract class DependencyBucketsTester {
        @Nested
        class ApiDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
            @Override
            public GradleRunner runner() {
                return runner;
            }

            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
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

            @Test
            void testCompileClasspathWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName")));
            }

            @Test
            void testApiElementsWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("apiElementsConfigurationName")));
            }

            @Test
            void testRuntimeClasspathWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName")));
            }

            @Test
            void testRuntimeElementsWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("runtimeElementsConfigurationName")));
            }
        }

        @Nested
        class CompileOnlyApiDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
            @Override
            public GradleRunner runner() {
                return runner;
            }

            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
            }

            @Override
            public ExpressionBuilder<?> bucketDsl() {
                return literal("gradlePlugin.dependencies.compileOnlyApi");
            }

            @Override
            public GradleBuildFile buildFile() {
                return buildFile;
            }

            @Override
            public GradleSettingsFile settingsFile() {
                return settingsFile;
            }

            @Test
            void testCompileClasspathWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName")));
            }

            @Test
            void testApiElementsWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("apiElementsConfigurationName")));
            }

            @Test
            void testRuntimeClasspathWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName"))));
            }

            @Test
            void testRuntimeElementsWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("runtimeElementsConfigurationName"))));
            }
        }

        @Nested
        class ImplementationDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
            @Override
            public GradleRunner runner() {
                return runner;
            }

            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
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

            @Test
            void testCompileClasspathWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName")));
            }

            @Test
            void testApiElementsWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("apiElementsConfigurationName"))));
            }

            @Test
            void testRuntimeClasspathWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName")));
            }

            @Test
            void testRuntimeElementsWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("runtimeElementsConfigurationName")));
            }
        }

        @Nested
        class CompileOnlyDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
            @Override
            public GradleRunner runner() {
                return runner;
            }

            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
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

            @Test
            void testCompileClasspathWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName")));
            }

            @Test
            void testApiElementsWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("apiElementsConfigurationName"))));
            }

            @Test
            void testRuntimeClasspathWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName"))));
            }

            @Test
            void testRuntimeElementsWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("runtimeElementsConfigurationName"))));
            }
        }

        @Nested
        class RuntimeOnlyDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
            @Override
            public GradleRunner runner() {
                return runner;
            }

            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
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

            @Test
            void testCompileClasspathWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName"))));
            }

            @Test
            void testApiElementsWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("apiElementsConfigurationName"))));
            }

            @Test
            void testRuntimeClasspathWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName")));
            }

            @Test
            void testRuntimeElementsWiring() {
                assertBucketDependencyIs(containedIn(sourceSetDsl().dot("runtimeElementsConfigurationName")));
            }
        }

        @Nested
        class AnnotationProcessorDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
            @Override
            public GradleRunner runner() {
                return runner;
            }

            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
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

            @Test
            void testCompileClasspathWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName"))));
            }

            @Test
            void testApiElementsWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("apiElementsConfigurationName"))));
            }

            @Test
            void testRuntimeClasspathWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName"))));
            }

            @Test
            void testRuntimeElementsWiring() {
                assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("runtimeElementsConfigurationName"))));
            }
        }
    }

    @Nested
    class DefaultPluginSourceSetTest extends DependencyBucketsTester {}

    @Nested
    class CustomPluginSourceSetTest extends DependencyBucketsTester {
        @BeforeEach
        void givenCustomPluginSourceSet() {
            buildFile.append(groovyDsl(
                    "gradlePlugin {",
                    "  pluginSourceSet(sourceSets.create('customMain'))",
                    "}"
            ));
        }
    }
}
