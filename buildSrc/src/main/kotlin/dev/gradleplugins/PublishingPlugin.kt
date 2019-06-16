package dev.gradleplugins

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import java.text.SimpleDateFormat
import java.util.*

class PublishingPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        applyPublishingPlugin()
        configurePublishingExtension()
        configureBintrayExtension()
    }

    private
    fun Project.applyPublishingPlugin() {
        apply<MavenPublishPlugin>()
        apply<BintrayPlugin>()
    }

    private
    fun Project.configurePublishingExtension() {
        val shadowJar = tasks.named<ShadowJar>("shadowJar")
        val sourcesJar = tasks.named<Jar>("sourcesJar")
        val groovydocJar = tasks.named<Jar>("groovydocJar")
        val javadocJar = tasks.named<Jar>("javadocJar")

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    artifact(shadowJar.get())
                    artifact(sourcesJar.get())
                    artifact(groovydocJar.get())
                    artifact(javadocJar.get())

                    pom {
                        name.set("Gradle TestKit Fixtures")
                        description.set(project.provider { project.description })
                        url.set("https://github.com/gradle-plugins/${rootProject.name}")
                        inceptionYear.set("2019")

                        scm {
                            url.set("https://github.com/gradle-plugins/${rootProject.name}")
                            connection.set("scm:https://github.com/gradle-plugins/${rootProject.name}.git")
                            developerConnection.set("scm:git://github.com/gradle-plugins/${rootProject.name}.git")
                        }

                        licenses {
                            license {
                                name.set("The Apache Software License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }

                        developers {
                            developer {
                                id.set("lacasseio")
                                name.set("Daniel Lacasse")
                                url.set("https://github.com/lacasseio")
                            }
                        }
                    }
                }
            }
        }
    }

    private
    fun Project.configureBintrayExtension() {
        val packageName = "dev.gradleplugins:${project.name}"

        configure<BintrayExtension> {
            user = resolveProperty("BINTRAY_USER", "bintrayUser")
            key = resolveProperty("BINTRAY_KEY", "bintrayKey")
            setPublications("mavenJava")
            publish = true

            pkg(closureOf<BintrayExtension.PackageConfig> {
                repo = "maven"
                userOrg = "gradle-plugins"
                name = packageName
                desc = project.description
                websiteUrl = "https://github.com/gradle-plugins/${rootProject.name}"
                issueTrackerUrl = "https://github.com/gradle-plugins/${rootProject.name}/issues"
                vcsUrl = "https://github.com/gradle-plugins/${rootProject.name}.git"
                setLicenses("Apache-2.0")
                setLabels("gradle", "gradle-plugins")
                publicDownloadNumbers = true
                githubRepo = "gradle-plugins/${rootProject.name}"

                version(closureOf<BintrayExtension.VersionConfig> {
                    released = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").format(Date())
                    vcsTag = "v${project.version}"

                    gpg(closureOf<BintrayExtension.GpgConfig> {
                        sign = false
                        passphrase = resolveProperty("GPG_PASSPHRASE", "gpgPassphrase")
                    })
//                    mavenCentralSync(closureOf<BintrayExtension.MavenCentralSyncConfig> {
//                        sync = true
//                        user = resolveProperty("MAVEN_CENTRAL_USER_TOKEN", "mavenCentralUserToken")
//                        password = resolveProperty("MAVEN_CENTRAL_PASSWORD", "mavenCentralPassword")
//                        close = "1"
//                    })
                })
            })
        }
    }

    private
    fun Project.resolveProperty(envVarKey: String, projectPropKey: String): String? {
        val propValue = System.getenv()[envVarKey]

        if(propValue != null) {
            return propValue
        }

        return findProperty(projectPropKey).toString()
    }
}