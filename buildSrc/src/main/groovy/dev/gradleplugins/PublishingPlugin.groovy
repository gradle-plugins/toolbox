package dev.gradleplugins

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import java.text.SimpleDateFormat
import java.util.*

class PublishingPlugin implements Plugin<Project> {
    void apply(Project project) {
        applyPublishingPlugin(project)
        configurePublishingExtension(project)
        configureBintrayExtension(project)
    }

    private void applyPublishingPlugin(Project project) {
        project.apply plugin: PublishingBasePlugin
        project.apply plugin: MavenPublishPlugin
        project.apply plugin: BintrayPlugin
    }

    private void configurePublishingExtension(Project project) {
//        var jarTask: TaskProvider<out Task> = tasks.named<Jar>("jar")
//        if (project.pluginManager.hasPlugin("com.github.johnrengelman.shadow")) {
//            jarTask = tasks.named<ShadowJar>("shadowJar")
//        }
        def sourcesJar = project.tasks.named("sourcesJar", Jar)
        def javadocJar = project.tasks.named("javadocJar", Jar)

        project.publishing {
            publications {
                create("mavenJava", MavenPublication) {
//                    artifact(jarTask.get())
                    artifact(sourcesJar.get())
                    if (project.pluginManager.hasPlugin("groovy")) {
                        def groovydocJar = project.tasks.named("groovydocJar", Jar)
                        artifact(groovydocJar.get())
                    }
                    artifact(javadocJar.get())

                    pom {
                        name.set("Gradle TestKit Fixtures")
                        description.set(project.provider { project.description })
                        inceptionYear.set("2019")

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

    private void configureBintrayExtension(Project project) {
        project.afterEvaluate {
//            val packageName = "${project.group}:${project.name}"

            project.extensions.configure(BintrayExtension) { bintray ->
                bintray.setPublications("mavenJava")

                bintray.pkg {
//                    name = packageName
                    setLabels("gradle", "gradle-plugins")
                    publicDownloadNumbers = true
                }
            }
        }
    }
}