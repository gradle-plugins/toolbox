package dev.gradleplugins;

import dev.gradleplugins.buildscript.SettingsBuildScript;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.fixtures.sample.GradlePluginElement;
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.test.fixtures.maven.M2Installation;
import dev.gradleplugins.test.fixtures.maven.MavenFileRepository;
import dev.gradleplugins.test.fixtures.maven.MavenModule;
import dev.gradleplugins.test.fixtures.maven.MavenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.gradleplugins.buildscript.blocks.ArtifactRepositoryStatements.mavenLocal;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradlePluginDevelopmentPublishingFunctionalTests {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(() -> testDirectory).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();
    GradleSettingsFile settingsFile;
    M2Installation m2;

    @BeforeEach
    void setup() {
        m2 = new M2Installation(testDirectory.toFile());
        settingsFile = GradleSettingsFile.inDirectory(testDirectory);
        settingsFile.append(groovyDsl("rootProject.name = 'project-under-test'"));
        runner = runner.configure(m2);
    }

    @Nested
    class GivenEnhancedPluginProject {
        GradleBuildFile buildFile;
        GradlePluginElement fixture = new JavaBasicGradlePlugin();

        @BeforeEach
        void setup() {
            buildFile = GradleBuildFile.inDirectory(testDirectory());
            buildFile.plugins(it -> {
                it.id("dev.gradleplugins.gradle-plugin-base");
                it.id("java-gradle-plugin");
                it.id("maven-publish");
            });
            buildFile.append(groovyDsl(
                    "group = 'com.example'",
                    "version = '1.0' // current plugin version"
            ));
            buildFile.append(groovyDsl(
                    "gradlePlugin {",
                    "  plugins {",
                    "    hello {",
                    "      id = '" + fixture.getPluginId() + "'",
                    "      implementationClass = 'com.example.BasicPlugin'",
                    "    }",
                    "  }",
                    "}"
            ));
            settingsFile.append(groovyDsl("include 'my-plugin'"));
            fixture.writeToProject(testDirectory().toFile());
        }

        Path testDirectory() {
            return testDirectory.resolve("my-plugin");
        }

        @Test
        void failsIfAutomaticPublishingIsEnabledWhenRegisteringPublication() {
            buildFile.append(groovyDsl(
                    "gradlePlugin {",
                    "  publishing.registerPublication('nightly', MavenPublication)",
                    "}",
                    "",
                    "tasks.register('verify')"
            ));

            assertTrue(runner.withTasks("verify").buildAndFail().getOutput().contains("> Please disable gradlePlugin.automatedPublishing!"));
        }

        @Test
        void failsIfAutomaticPublishingIsEnabledAndPublicationsExistsAfterProjectEvaluates() {
            buildFile.append(groovyDsl(
                    "gradlePlugin {",
                    "  automatedPublishing = false",
                    "  publishing.registerPublication('nightly', MavenPublication)",
                    "  automatedPublishing = true",
                    "}",
                    "",
                    "tasks.register('verify')"
            ));

            assertTrue(runner.withTasks("verify").buildAndFail().getOutput().contains("> Please disable gradlePlugin.automatedPublishing!"));
        }


        // Tester for publishing project depending on enhanced publication
        abstract class DependentPluginTester {
            GradleBuildFile buildFile;

            @BeforeEach
            void setup() {
                settingsFile.append(groovyDsl("include 'other-plugin'"));
                buildFile = GradleBuildFile.inDirectory(testDirectory());
                buildFile.plugins(it -> {
                    it.id("java-gradle-plugin");
                    it.id("maven-publish");
                });
                new JavaBasicGradlePlugin().writeToProject(testDirectory().toFile());
                buildFile.append(groovyDsl(
                        "group = 'com.example'",
                        "version = '4.2'",
                        "",
                        "dependencies {",
                        "  implementation project(':my-plugin')",
                        "}",
                        "",
                        "gradlePlugin {",
                        "  plugins {",
                        "    bob {",
                        "      id = 'com.example.foo'",
                        "      implementationClass = 'com.example.BasicPlugin'",
                        "    }",
                        "  }",
                        "}"
                ));
            }

            public Path testDirectory() {
                return testDirectory.resolve("other-plugin");
            }

            @Test
            void usesProjectCoordinateWhenPublishingNormally() throws IOException {
                runner.withTasks(":other-plugin:publishToMavenLocal").build();
                assertTrue(new String(Files.readAllBytes(Paths.get(m2.mavenRepo().module("com.example", "other-plugin", "4.2").getPom().getUri()))).contains("<version>1.0</version>"));
            }

            abstract class PublishDependentPluginViaPublicationTester {
                public abstract MavenModule publishedModule();
                public abstract MavenModule dependencyModule();
                public abstract PublishTaskBuilder publishTasks();

                @Test
                void usesPublicationCoordinateWhenPublishingTogether() throws IOException {
                    runner.withTasks(":my-plugin:" + publishTasks().publishLifecycleTaskName(), ":other-plugin:publishToMavenLocal").build();
                    assertTrue(new String(Files.readAllBytes(Paths.get(publishedModule().getPom().getUri()))).contains("<version>" + dependencyModule().getVersion() + "</version>"));
                }
            }
        }

        @Nested
        class GivenPublications {
            @BeforeEach
            void setup() {
                buildFile.append(groovyDsl(
                        "gradlePlugin {",
                        "  automatedPublishing = false",
                        "  publishing.registerPublication('snapshot', MavenPublication) {",
                        "    version = '1.1-SNAPSHOT'",
                        "  }",
                        "  publishing.registerPublication('releaseCandidate', MavenPublication) {",
                        "    version = '1.1-rc-1'",
                        "  }",
                        "}"
                ));
            }

            @Nested
            class GivenDependentPublishing extends DependentPluginTester {
                @Nested
                class SnapshotDependentPublicationTest extends PublishDependentPluginViaPublicationTester {
                    @Override
                    public MavenModule publishedModule() {
                        return m2.mavenRepo().module("com.example", "other-plugin", "4.2");
                    }

                    @Override
                    public MavenModule dependencyModule() {
                        return m2.mavenRepo().module("com.example", "my-plugin", "1.1-SNAPSHOT");
                    }

                    @Override
                    public PublishTaskBuilder publishTasks() {
                        return new PublishTaskBuilder("snapshot");
                    }
                }

                @Nested
                class ReleaseCandidateDependentPublicationTest extends PublishDependentPluginViaPublicationTester {
                    @Override
                    public MavenModule publishedModule() {
                        return m2.mavenRepo().module("com.example", "other-plugin", "4.2");
                    }

                    @Override
                    public MavenModule dependencyModule() {
                        return m2.mavenRepo().module("com.example", "my-plugin", "1.1-rc-1");
                    }

                    @Override
                    public PublishTaskBuilder publishTasks() {
                        return new PublishTaskBuilder("releaseCandidate");
                    }
                }
            }

            @Nested
            class GivenConsumerProject {
                GradleBuildFile buildFile;
                SettingsBuildScript settingsFile;

                @BeforeEach
                void setup() {
                    buildFile = GradleBuildFile.inDirectory(testDirectory());
                    settingsFile = GradleSettingsFile.inDirectory(testDirectory());
                    runner.withTasks("publishReleaseCandidateToMavenLocal").build();
                    settingsFile.pluginManagement(it -> it.repositories(t -> t.add(mavenLocal())));
                    buildFile.plugins(it -> it.id(fixture.getPluginId(), "1.1-rc-1"));
                    buildFile.append(groovyDsl("tasks.register('verify')"));
                }

                public Path testDirectory() {
                    return testDirectory.resolve("consumer");
                }

                @Test
                void bob() {
                    runner.inDirectory(testDirectory()).withTasks("verify").build();
                }
            }

            @Nested
            class PublishSnapshotPublicationToMavenLocalTest extends PublishTester {
                @Override
                MavenModule publishedModule() {
                    return m2.mavenRepo().module("com.example", "my-plugin", "1.1-SNAPSHOT");
                }

                @Override
                PublishTaskBuilder publishTasks() {
                    return new PublishTaskBuilder("snapshot");
                }
            }

            @Nested
            class PublishReleaseCandidatePublicationToMavenLocalTest extends PublishTester {
                @Override
                MavenModule publishedModule() {
                    return m2.mavenRepo().module("com.example", "my-plugin", "1.1-rc-1");
                }

                @Override
                PublishTaskBuilder publishTasks() {
                    return new PublishTaskBuilder("releaseCandidate");
                }
            }

            @Nested
            class PublishReleaseCandidatePublicationToCustomMavenRepository extends PublishTester {
                MavenRepository mavenRepository;

                @BeforeEach
                void setup() {
                    mavenRepository = new MavenFileRepository(testDirectory.resolve("my-repo"));
                    buildFile.append(groovyDsl(
                            "publishing.repositories {",
                            "  maven {",
                            "    name = 'RcRepo'",
                            "    url = '" + mavenRepository.getUri() + "'",
                            "  }",
                            "}"
                    ));
                }

                @Override
                MavenModule publishedModule() {
                    return mavenRepository.module("com.example", "my-plugin", "1.1-rc-1");
                }

                @Override
                PublishTaskBuilder publishTasks() {
                    return new PublishTaskBuilder("releaseCandidate", "rcRepoRepository");
                }
            }

            @Test
            void failsWhenPublishingMultiplePublicationSpec() {
                runner.withTasks("publishSnapshotToMavenLocal", "publishReleaseCandidateToMavenLocal").buildAndFail();
            }
        }
    }

    /**
     * Tester for basic publication spec contract:
     * <ul>
     *     <li>Never attached to {@literal publish} lifecycle task</li>
     *     <li>Can publish to repository</li>
     * </ul>
     */
    abstract class PublishTester {
        abstract MavenModule publishedModule();
        abstract PublishTaskBuilder publishTasks();

        @Test
        void doesNotRegisterPluginPublicationsUnderPublishLifecycleTask() {
            assertEquals(singletonList(":my-plugin:publish"), runner.withTasks("publish").build().getExecutedTaskPaths());
        }

        @Test// TODO: change test name to reflect any repository
        void canPublishToMavenLocal() {
            assertThat(runner.withTasks(publishTasks().publishLifecycleTaskName()).build().getExecutedTaskPaths(),
                    hasItems(":my-plugin:" + publishTasks().publishPublicationTaskName("helloPluginMarkerMaven"), ":my-plugin:" + publishTasks().publishPublicationTaskName("pluginMaven"), ":my-plugin:" + publishTasks().publishLifecycleTaskName()));
            assertTrue(publishedModule().isPublished());
            // TODO: tests other publications
        }
    }

    public static final class PublishTaskBuilder {
        private final String publicationName;
        private final String repositoryName;

        PublishTaskBuilder(String publicationName) {
            this(publicationName, "mavenLocal");
        }

        PublishTaskBuilder(String publicationName, String repositoryName) {
            this.publicationName = publicationName;
            this.repositoryName = repositoryName;
        }

        String publishLifecycleTaskName() {
            return "publish" + capitalized(publicationName) + "To" + capitalized(repositoryName);
        }

        String publishPublicationTaskName(String name) {
            return "publish" + capitalized(publicationName) + capitalized(name) + "PublicationTo" + capitalized(repositoryName);
        }

        private String capitalized(String s) {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }
}
