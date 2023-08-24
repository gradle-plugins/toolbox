package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.ast.expressions.AssignmentExpression.assign;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.not;
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
        public Expression gradleApiDsl(String version) {
            return testSuiteDsl().dot("dependencies.gradleApi").call(string(version));
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
        public Expression gradleTestKitDsl(String version) {
            return testSuiteDsl().dot("dependencies.gradleTestKit").call(string(version));
        }

        @Override
        public Expression gradleTestKitDsl() {
            return testSuiteDsl().dot("dependencies.gradleTestKit()");
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
        public Expression projectDsl(String projectPath) {
            return testSuiteDsl().dot("dependencies.project").call(string(projectPath));
        }

        @Override
        public Expression projectDsl() {
            return testSuiteDsl().dot("dependencies.project()");
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
    class ImplementationDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        public ExpressionBuilder<?> sourceSetDsl() {
            return testSuiteDsl().dot("sourceSet.get()");
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

        @Test
        void testCompileClasspathWiring() {
            assertBucketDependencyIs(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName")));
        }

        @Test
        void testRuntimeClasspathWiring() {
            assertBucketDependencyIs(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName")));
        }
    }

    @Nested
    class CompileOnlyDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        public ExpressionBuilder<?> sourceSetDsl() {
            return testSuiteDsl().dot("sourceSet.get()");
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

        @Test
        void testCompileClasspathWiring() {
            assertBucketDependencyIs(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName")));
        }

        @Test
        void testRuntimeClasspathWiring() {
            assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName"))));
        }
    }

    @Nested
    class RuntimeOnlyDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        public ExpressionBuilder<?> sourceSetDsl() {
            return testSuiteDsl().dot("sourceSet.get()");
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

        @Test
        void testCompileClasspathWiring() {
            assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName"))));
        }

        @Test
        void testRuntimeClasspathWiring() {
            assertBucketDependencyIs(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName")));
        }
    }

    @Nested
    class AnnotationProcessorDependencyBucketTest extends DependencyBucketTester implements DependencyWiringTester {
        @Override
        public GradleRunner runner() {
            return GradlePluginDevelopmentTestSuiteDependenciesTester.this.runner();
        }

        public ExpressionBuilder<?> sourceSetDsl() {
            return testSuiteDsl().dot("sourceSet.get()");
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

        @Test
        void testCompileClasspathWiring() {
            assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("compileClasspathConfigurationName"))));
        }

        @Test
        void testRuntimeClasspathWiring() {
            assertBucketDependencyIs(not(containedIn(sourceSetDsl().dot("runtimeClasspathConfigurationName"))));
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
            buildFile().append(val("bucketUnderTest", assign(bucketDsl())));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert !bucketUnderTest.asConfiguration.get().isCanBeConsumed()",
                    "    assert bucketUnderTest.asConfiguration.get().isCanBeResolved()",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void hasDescription() {
            buildFile().append(val("bucketUnderTest", assign(bucketDsl())));
            buildFile().append(val("testSuiteUnderTest", assign(testSuiteDsl())));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert bucketUnderTest.asConfiguration.get().description == \"Plugin under test metadata for source set '${testSuiteUnderTest.name}'.\"",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void hasJavaRuntimeUsageAttribute() {
            buildFile().append(val("bucketUnderTest", assign(bucketDsl())));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert bucketUnderTest.asConfiguration.get().attributes.getAttribute(Usage.USAGE_ATTRIBUTE).name == 'java-runtime'",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }
    }
}
