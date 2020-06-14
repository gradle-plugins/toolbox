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
                compileOnly {
                    canBeConsumed = false
                    canBeResolved = false
                }
                apiElements {
                    canBeResolved = false
                    canBeConsumed = true
                    extendsFrom compileOnly
                    outgoing.artifact(generateGradleApiJarTask.flatMap { it.outputFile })
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_API))
                        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EXTERNAL))
                        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
                        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, Integer.parseInt(toMinimumJavaVersion(VersionNumber.parse(gradleVersion)).majorVersion))
                    }

                }

                runtimeOnly {
                    canBeConsumed = false
                    canBeResolved = false
                }

                runtimeElements {
                    canBeResolved = false
                    canBeConsumed = true
                    extendsFrom runtimeOnly
                    outgoing.artifact(generateGradleApiJarTask.flatMap { it.outputFile })
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
                        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EXTERNAL))
                        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
                        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, Integer.parseInt(toMinimumJavaVersion(VersionNumber.parse(gradleVersion)).majorVersion))
                    }

                }
                sourcesElements {
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
                compileOnly provider { "org.codehaus.groovy:groovy:${toGroovyVersion(VersionNumber.parse(gradleVersion))}" }
                runtimeOnly provider { "org.codehaus.groovy:groovy-all:${toGroovyVersion(VersionNumber.parse(gradleVersion))}" }

                def kotlinVersion = toKotlinVersion(VersionNumber.parse(gradleVersion))
                if (kotlinVersion.present) {
                    runtimeOnly "org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion.get()}"
                }
            }

            def adhocComponent = softwareComponentFactory.adhoc('gradleApi');
            adhocComponent.addVariantsFromConfiguration(configurations.apiElements) {}
            adhocComponent.addVariantsFromConfiguration(configurations.runtimeElements) {}
            adhocComponent.addVariantsFromConfiguration(configurations.sourcesElements) {}
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

            tasks.register('uploadGradleApiJars') {
                dependsOn('bintrayUpload')
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
    private static String toGroovyVersion(VersionNumber gradleVersion) {
        // Use `find ~/.gradle/wrapper -name "groovy-all-*"` once the distribution was downloaded locally
        switch (String.format("%d.%d", gradleVersion.getMajor(), gradleVersion.getMinor())) {
            case "1.12":
                return "1.8.6";
            case "2.0":
                return "2.3.3";
            case "2.1":
            case "2.2":
                return "2.3.6";
            case "2.3":
                return "2.3.9";
            case "2.4":
            case "2.5":
            case "2.6":
            case "2.7":
                return "2.3.10";
            case "2.8":
            case "2.9":
            case "2.10":
            case "2.11":
            case "2.12":
            case "2.13":
            case "2.14":
                return "2.4.4";
            case "3.0":
            case "3.1":
            case "3.2":
            case "3.3":
            case "3.4":
                return "2.4.7";
            case "3.5":
                return "2.4.10";
            case "4.0":
            case "4.1":
                return "2.4.11";
            case "4.2":
            case "4.3":
            case "4.4":
            case "4.5":
            case "4.6":
            case "4.7":
            case "4.8":
            case "4.9":
                return "2.4.12";
            case "4.10":
                return "2.4.15";
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
            case "6.4":
                return "2.5.10"; //"org.gradle.groovy:groovy-all:1.3-2.5.10";
            case "6.5":
                return "2.5.11"; //"org.gradle.groovy:groovy-all:1.3-2.5.11";
            default:
                throw new IllegalArgumentException(String.format("Unknown Groovy version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox.", gradleVersion.toString()));
        }
    }

    private static Optional<String> toKotlinVersion(VersionNumber gradleVersion) {
        // Use `find ~/.gradle/wrapper -name "kotlin-stdlib-*"` once the distribution was downloaded locally
        switch (String.format("%d.%d", gradleVersion.getMajor(), gradleVersion.getMinor())) {
            case "2.0":
            case "2.1":
            case "2.2":
            case "2.3":
            case "2.4":
            case "2.5":
            case "2.6":
            case "2.7":
            case "2.8":
            case "2.9":
            case "2.10":
            case "2.11":
            case "2.12":
            case "2.13":
            case "2.14":
                return Optional.empty();
            case "3.0":
                return Optional.of("1.1-M01"); // Does not exists
            case "3.1":
            case "3.2":
                return Optional.of("1.1.0-dev-2053"); // Found in https://dl.bintray.com/kotlin/kotlin-dev/
            case "3.3":
            case "3.4":
                return Optional.of("1.1-M02-8"); // Technically, the version in Gradle distribution is 1.1-M02, but only 1.1.-M02-8 in https://dl.bintray.com/kotlin/kotlin-dev/ can be found.
            case "3.5":
            case "4.0":
                return Optional.of("1.1.0");
            case "4.1":
                return Optional.of("1.1.3-2");
            case "4.2":
                return Optional.of("1.1.4-3");
            case "4.3":
            case "4.4":
                return Optional.of("1.1.51");
            case "4.5":
                return Optional.of("1.2.0");
            case "4.6":
                return Optional.of("1.2.21");
            case "4.7":
                return Optional.of("1.2.31");
            case "4.8":
            case "4.9":
                return Optional.of("1.2.41");
            case "4.10":
                return Optional.of("1.2.61");
            case "5.0":
                return Optional.of("1.3.10");
            case "5.1":
                return Optional.of("1.3.11");
            case "5.2":
                return Optional.of("1.3.20");
            case "5.3":
            case "5.4":
                return Optional.of("1.3.21");
            case "5.5":
                return Optional.of("1.3.31");
            case "5.6":
                return Optional.of("1.3.41");
            case "6.0":
                return Optional.of("1.3.50");
            case "6.1":
            case "6.2":
                return Optional.of("1.3.61");
            case "6.3":
                return Optional.of("1.3.70");
            case "6.4":
                return Optional.of("1.3.71");
            case "6.5":
                return Optional.of("1.3.72");
            default:
                throw new IllegalArgumentException(String.format("Unknown Kotlin version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox.", gradleVersion.toString()));
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
