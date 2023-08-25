package dev.gradleplugins;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.fixtures.sample.GradlePluginApi;
import dev.gradleplugins.fixtures.sample.GradlePluginElement;
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.test.fixtures.maven.M2Installation;
import dev.gradleplugins.testers.DependencyWiringTester;
import dev.gradleplugins.testers.GradlePluginApiTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.not;

class GradlePluginDevelopmentPluginApiFunctionalTests {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();
    M2Installation m2;

    @BeforeEach
    void configureM2() {
        m2 = new M2Installation(testDirectory.toFile());
        runner = runner.configure(m2);
    }

    abstract class ConsumerTester {
        GradleBuildFile buildFile;
        GradleSettingsFile settingsFile;

        public Path testDirectory() {
            return testDirectory;
        }

        public GradleRunner runner() {
            return runner.inDirectory(testDirectory());
        }

        public abstract GradlePluginElement fixture();

        public abstract void publishPluginToMavenLocal();

        @BeforeEach
        void givenProject() {
            buildFile = GradleBuildFile.inDirectory(testDirectory());
            settingsFile = GradleSettingsFile.inDirectory(testDirectory());
        }


        abstract class AsPluginLibraryTester {
            @Test
            void canBuildUsingApi() throws IOException {
                Files.write(Files.createDirectories(testDirectory().resolve("src/main/java")).resolve("MyAppExtension.java"), Arrays.asList(
                        "import com.example.api.MyExtension;",
                        "",
                        "abstract class MyAppExtension implements MyExtension {}"
                ));

                runner().withTasks("build").build();
            }

            @Test
            void failsBuildWhenUsingImplementationClasses() throws IOException {
                Files.write(Files.createDirectories(testDirectory().resolve("src/main/java")).resolve("MyBrokenPlugin.java"), Arrays.asList(
                        "import com.example.BasicPlugin;",
                        "",
                        "abstract class MyBrokenPlugin extends BasicPlugin {}"
                ));

                runner().withTasks("build").buildAndFail();
            }
        }


        @Nested
        class Composite extends AsPluginLibraryTester {
            @BeforeEach
            void givenComposite() {
                settingsFile.append(groovyDsl("includeBuild 'my-plugin'"));
                buildFile.plugins(it -> it.id("java-library"));
                buildFile.append(groovyDsl(
                        "dependencies {",
                        "  implementation 'com.example:my-plugin'",
                        "}"
                ));
            }
        }

        @Nested
        class Published extends AsPluginLibraryTester {
            @BeforeEach
            void setup() {
                buildFile.plugins(it -> it.id("java-library"));
                buildFile.append(groovyDsl(
                        "repositories {",
                        "  mavenLocal()",
                        "}",
                        "dependencies {",
                        "  implementation 'com.example:my-plugin:latest.release'",
                        "}"
                ));

                publishPluginToMavenLocal();
            }
        }

        @Nested
        class AsLocalPlugin {
            @BeforeEach
            void includePluginBuild() {
                settingsFile.append(groovyDsl(
                        "pluginManagement {",
                        "  includeBuild 'my-plugin'",
                        "}"
                ));

                // Apply plugin under test
                buildFile.plugins(it -> it.id(fixture().getPluginId()));
            }

            @Test
            void canUseApiClasses() {
                buildFile.append(groovyDsl(
                        "import com.example.api.MyExtension",
                        "",
                        "abstract class MyAppExtension implements MyExtension {}",
                        "",
                        "project.objects.newInstance(MyAppExtension)",
                        "tasks.register('verify')"
                ));
                runner().withTasks("verify").build();
            }

            @Test // Note that Gradle build script doesn't make the distinction between API and runtime
            void canUseImplementationClasses() {
                buildFile.append(groovyDsl(
                        "import com.example.BasicPlugin;",
                        "",
                        "abstract class MyPlugin extends BasicPlugin {}",
                        "",
                        "apply plugin: MyPlugin",
                        "tasks.register('verify')"
                ));
                runner().withTasks("verify").build();
            }
        }
    }

    @Nested
    class DifferentSourceSetTest extends GradlePluginApiTester {
        @Override
        public GradleRunner runner() {
            return runner.inDirectory(testDirectory());
        }

        @Override
        public Path testDirectory() {
            return testDirectory.resolve("my-plugin");
        }

        @Override
        public GradlePluginApi fixture() {
            return new GradlePluginApi(new JavaBasicGradlePlugin()).withSourceSetName("api");
        }

        @BeforeEach
        void setup() {
            pluginBuildFile().plugins(it -> {
                it.id("dev.gradleplugins.gradle-plugin-base");
                it.id("java-gradle-plugin");
            });
            pluginBuildFile().append(groovyDsl(
                    "gradlePlugin {",
                    "  api {",
                    "    sourceSet = sourceSets.register('api')",
                    "  }",
                    "}"
            ));
        }


        @Nested
        class Implementation_ApiSourceSetTest implements DependencyWiringTester {
            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
            }

            @Override
            public ExpressionBuilder<?> bucketDsl() {
                return literal("dependencies.apiImplementation");
            }

            @Override
            public GradleBuildFile buildFile() {
                return DifferentSourceSetTest.this.pluginBuildFile();
            }

            @Override
            public GradleRunner runner() {
                return DifferentSourceSetTest.this.runner();
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
        class CompileOnlyApi_ApiSourceSetTest implements DependencyWiringTester {
            @BeforeEach
            void given() {
                buildFile().append(groovyDsl(
                        "def compileOnlyApi = configurations.create(sourceSets.api.compileOnlyApiConfigurationName) {",
                        "  canBeResolved = false",
                        "  canBeConsumed = false",
                        "}",
                        "configurations.named(sourceSets.api.compileClasspathConfigurationName) {",
                        "  extendsFrom compileOnlyApi",
                        "}"
                ));
            }

            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
            }

            @Override
            public ExpressionBuilder<?> bucketDsl() {
                return literal("dependencies.apiCompileOnlyApi");
            }

            @Override
            public GradleBuildFile buildFile() {
                return DifferentSourceSetTest.this.pluginBuildFile();
            }

            @Override
            public GradleRunner runner() {
                return DifferentSourceSetTest.this.runner();
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
        class CompileOnly_ApiSourceSetTest implements DependencyWiringTester {
            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
            }

            @Override
            public ExpressionBuilder<?> bucketDsl() {
                return literal("dependencies.apiCompileOnly");
            }

            @Override
            public GradleBuildFile buildFile() {
                return DifferentSourceSetTest.this.pluginBuildFile();
            }

            @Override
            public GradleRunner runner() {
                return DifferentSourceSetTest.this.runner();
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
        class RuntimeOnly_ApiSourceSetTest implements DependencyWiringTester {
            public ExpressionBuilder<?> sourceSetDsl() {
                return literal("gradlePlugin.pluginSourceSet");
            }

            @Override
            public ExpressionBuilder<?> bucketDsl() {
                return literal("dependencies.apiRuntimeOnly");
            }

            @Override
            public GradleBuildFile buildFile() {
                return DifferentSourceSetTest.this.pluginBuildFile();
            }

            @Override
            public GradleRunner runner() {
                return DifferentSourceSetTest.this.runner();
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
        class ConsumerTest extends ConsumerTester {
            @Override
            public GradleRunner runner() {
                return runner.inDirectory(testDirectory);
            }

            @Override
            public GradlePluginElement fixture() {
                return DifferentSourceSetTest.this.fixture();
            }

            @Override
            public void publishPluginToMavenLocal() {
                DifferentSourceSetTest.this.pluginBuildFile().plugins(it -> it.id("maven-publish"));
                DifferentSourceSetTest.this.runner().withTasks("publishToMavenLocal").build();
            }
        }
    }

    @Nested
    class SameSourceSetDifferentJarTest extends GradlePluginApiTester {
        @Override
        public GradleRunner runner() {
            return runner.inDirectory(testDirectory());
        }

        @Override
        public Path testDirectory() {
            return testDirectory.resolve("my-plugin");
        }

        @Override
        public GradlePluginApi fixture() {
            return new GradlePluginApi(new JavaBasicGradlePlugin()).withSourceSetName("main");
        }

        @BeforeEach
        void setup() {
            pluginBuildFile().plugins(it -> {
                it.id("dev.gradleplugins.gradle-plugin-base");
                it.id("java-gradle-plugin");
            });
            pluginBuildFile().append(groovyDsl(
                    "tasks.named('jar', Jar) {",
                    "  exclude('**/api/**')",
                    "}",
                    "gradlePlugin {",
                    "  api {",
                    "    jarTask = tasks.register('apiJar', Jar) {",
                    "      archiveClassifier = 'api'",
                    "      from(sourceSet.get().output)",
                    "      include('**/api/**')",
                    "    }",
                    "  }",
                    "}"
            ));
        }

        @Nested
        class ConsumerTest extends ConsumerTester {
            @Override
            public GradleRunner runner() {
                return runner.inDirectory(testDirectory);
            }

            @Override
            public GradlePluginElement fixture() {
                return SameSourceSetDifferentJarTest.this.fixture();
            }

            @Override
            public void publishPluginToMavenLocal() {
                SameSourceSetDifferentJarTest.this.pluginBuildFile().plugins(it -> it.id("maven-publish"));
                SameSourceSetDifferentJarTest.this.runner().withTasks("publishToMavenLocal").build();
            }
        }
    }
}
