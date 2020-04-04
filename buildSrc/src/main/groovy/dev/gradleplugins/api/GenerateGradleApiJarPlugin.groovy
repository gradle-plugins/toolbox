package dev.gradleplugins.api

import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.*
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.artifact.FileBasedMavenArtifact
import org.gradle.util.VersionNumber

import javax.annotation.Nullable
import javax.inject.Inject
import java.text.SimpleDateFormat

abstract class GenerateGradleApiJarPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def gradleVersion = project.name

        project.with {
            apply plugin: 'lifecycle-base'

            group = 'dev.gradleplugins'
            version = gradleVersion

            def generateGradleApiJarTask = tasks.register("generateGradleApi", GenerateGradleApiJar) { task ->
                task.getVersion().set(gradleVersion)
                task.getOutputFile().set(layout.buildDirectory.file("generated-gradle-jars/gradle-api.jar"));
                task.getOutputSourceFile().set(layout.buildDirectory.file("generated-gradle-jars/gradle-api-sources.jar"));
            }

            configurations {
                api {
                    canBeConsumed = false
                    canBeResolved = false
                }
                apiJarElements {
                    canBeResolved = false
                    canBeConsumed = true
                    extendsFrom api
                    outgoing.artifact(generateGradleApiJarTask.flatMap { it.outputFile })
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_API))
                        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EXTERNAL))
                        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
                        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, Integer.parseInt(toMinimumJavaVersion(VersionNumber.parse(gradleVersion)).majorVersion))
                    }

                }
                apiSourcesElements {
                    canBeResolved = false
                    canBeConsumed = true
                    outgoing.artifact(generateGradleApiJarTask.flatMap { it.outputSourceFile }) {
                        classifier = 'sources'
                    }
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
                        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.DOCUMENTATION))
                        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EXTERNAL))
                        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType, DocsType.SOURCES))
                        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, Integer.parseInt(toMinimumJavaVersion(VersionNumber.parse(gradleVersion)).majorVersion))
                    }
                }
            }
            dependencies {
                constraints {
                    api "org.codehaus.groovy:groovy-all:${toGroovyVersion(VersionNumber.parse(gradleVersion))}"
                }
                api "org.codehaus.groovy:groovy:${toGroovyVersion(VersionNumber.parse(gradleVersion))}"
            }

            def adhocComponent = softwareComponentFactory.adhoc('gradleApi');
            adhocComponent.addVariantsFromConfiguration(configurations.apiJarElements) {}
            adhocComponent.addVariantsFromConfiguration(configurations.apiSourcesElements) {}
            project.getComponents().add(adhocComponent);

            apply plugin: 'maven-publish'
            publishing {
                publications {
                    gradleApi(MavenPublication) {
                        from components.gradleApi
                        version = gradleVersion
                        groupId = project.group
                        artifactId = 'gradle-api'
                        pom {
                            name = "Gradle ${gradleVersion} API";
                            description = project.provider { project.description }
                            developers {
                                developer {
                                    id = "gradle"
                                    name = "Gradle Inc."
                                    url = "https://github.com/gradle"
                                }
                            }
                        }
                    }
                }
            }

            apply plugin: 'com.jfrog.bintray'
            // Temporary workaround for https://github.com/bintray/gradle-bintray-plugin/issues/229
            PublishingExtension publishing = project.extensions.getByType(PublishingExtension)
            project.tasks.withType(BintrayUploadTask).configureEach {
                doFirst {
                    publishing.publications.withType(MavenPublication).each { publication ->
                        File moduleFile = project.buildDir.toPath()
                                .resolve("publications/${publication.name}/module.json").toFile()

                        if (moduleFile.exists()) {
                            publication.artifact(new FileBasedMavenArtifact(moduleFile) {
                                @Override
                                protected String getDefaultExtension() {
                                    return "module"
                                }
                            })
                        }
                    }
                }
            }

            afterEvaluate {
                bintray {
                    user = resolveProperty(project, "BINTRAY_USER", "dev.gradleplugins.bintray.user")
                    key = resolveProperty(project, "BINTRAY_KEY", "dev.gradleplugins.bintray.key")
                    publications = publishing.publications.collect { it.name }

                    publish = true
                    override = true

                    pkg {
                        repo = 'distributions'
                        name = 'dev.gradleplugins:gradle-api'
                        desc = project.description
                        userOrg = 'gradle-plugins'
                        websiteUrl = 'https://gradleplugins.dev'
                        issueTrackerUrl = 'https://github.com/gradle-plugins/toolbox/issues'
                        vcsUrl = 'https://github.com/gradle-plugins/toolbox.git'
                        labels = ['gradle', 'gradle-api', 'gradle-plugins']
                        licenses = ['Apache-2.0']
                        publicDownloadNumbers = false

                        version {
                            released = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").format(new Date())
                            // TODO: Sign artifacts
                            gpg {
                                sign = false
                                passphrase = resolveProperty(project, "GPG_PASSPHRASE", "dev.gradleplugins.bintray.gpgPassphrase")
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject
    protected abstract SoftwareComponentFactory getSoftwareComponentFactory();

    @Nullable
    private static String resolveProperty(Project project, String envVarKey, String projectPropKey) {
        Object propValue = System.getenv().get(envVarKey);

        if (propValue != null) {
            return propValue.toString();
        }

        propValue = project.findProperty(projectPropKey);
        if (propValue != null) {
            return propValue.toString();
        }

        return null;
    }

    // TODO: Use the mapping from the groovy-gradle-plugin
    private static String toGroovyVersion(VersionNumber version) {
        // Use `find ~/.gradle/wrapper -name "groovy-all-*"`
        // TODO: Complete this mapping
        switch (String.format("%d.%d", version.getMajor(), version.getMinor())) {
            case "1.12":
                return "1.8.6";
            case "2.14":
                return "2.4.4";
            case "3.0":
                return "2.4.7";
            case "3.5":
                return "2.4.10";
            case "4.0":
                return "2.4.11";
            case "4.3":
                return "2.4.12";
            case "5.0":
            case "5.1":
            case "5.2":
            case "5.3":
            case "5.4":
            case "5.5":
                return "2.5.4"; //"org.gradle.groovy:groovy-all:1.0-2.5.4";
            case "5.6":
                return "2.5.4"; //"org.gradle.groovy:groovy-all:1.3-2.5.4";
            case "6.0":
            case "6.1":
            case "6.2":
                return "2.5.8"; //"org.gradle.groovy:groovy-all:1.3-2.5.8";
            case "6.3":
                return "2.5.10"; //"org.gradle.groovy:groovy-all:1.3-2.5.10";
            default:
                throw new IllegalArgumentException("Version not known at the time, please check groovy-all version");
        }
    }

    // TODO: Use the mapping from development plugins
    private static JavaVersion toMinimumJavaVersion(VersionNumber version) {
        switch (version.getMajor()) {
            case 0:
                throw new UnsupportedOperationException("I didn't have time to figure out what is the minimum Java version for Gradle version below 1.0. Feel free to open an issue and look into that for me.");
            case 1:
                return JavaVersion.VERSION_1_5;
            case 2:
                return JavaVersion.VERSION_1_6;
            case 3:
            case 4:
                return JavaVersion.VERSION_1_7;
            case 5:
            case 6:
                return JavaVersion.VERSION_1_8;
            default:
                throw new IllegalArgumentException("Version not known at the time, please check what Java version is supported");
        }
    }
}
