package dev.gradleplugins.api

import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.*
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.artifact.FileBasedMavenArtifact

import javax.annotation.Nullable
import javax.inject.Inject
import java.text.SimpleDateFormat

import static dev.gradleplugins.GradleRuntimeCompatibility.*

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
                        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, Integer.parseInt(minimumJavaVersionFor(gradleVersion).majorVersion))
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
                        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, Integer.parseInt(minimumJavaVersionFor(gradleVersion).majorVersion))
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
                        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, Integer.parseInt(minimumJavaVersionFor(gradleVersion).majorVersion))
                    }
                }
            }
            dependencies {
                compileOnly provider { "org.codehaus.groovy:groovy:${groovyVersionOf(gradleVersion)}" }
                runtimeOnly provider { "org.codehaus.groovy:groovy-all:${groovyVersionOf(gradleVersion)}" }

                def kotlinVersion = kotlinVersionOf(gradleVersion)
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
}
