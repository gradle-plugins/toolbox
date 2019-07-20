import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.gradleplugins.kotlin-gradle-plugin")
    `kotlin-dsl`
    dev.gradleplugins.experimental.`shaded-artifact`
}

// Supported by the development plugins
tasks.named("publishPlugins") {
    dependsOn("shadowJar")
}
//afterEvaluate {
//    publishing {
//        publications {
//            withType<MavenPublication> {
//                if (name == "pluginMaven") {
//                    setArtifacts(listOf(tasks.getByName("shadowJar")))
//                }
//            }
//        }
//    }
//}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.gradle.publish:plugin-publish-plugin:0.10.1")
}

val generatorTask = tasks.register("createVersionInformation") {
    outputs.dir(project.layout.buildDirectory.dir("generatedSources"))
    inputs.property("version", project.version)
    inputs.property("group", project.group)
    inputs.property("name", project.project(":gradle-testkit-fixtures").name)
    doLast {
        project.layout.buildDirectory.file("generatedSources/TestFixtures.kt").get().asFile.writeText("""package dev.gradleplugins.internal

object TestFixtures {
    var released = ${!project.version.toString().contains("-SNAPSHOT")}
    var notation = "${project.group}:${project.project(":gradle-testkit-fixtures").name}:${project.version}"
}
""")
    }
}
tasks.named<KotlinCompile>("compileKotlin") {
    dependsOn(generatorTask)
}
sourceSets.main.configure {
    withConvention(KotlinSourceSet::class) {
        kotlin.srcDir(project.layout.buildDirectory.dir("generatedSources"))
    }
}

gradlePlugin {
    plugins {
        create("javaGradlePluginDevelopment") {
            id = "dev.gradleplugins.java-gradle-plugin"
            implementationClass = "dev.gradleplugins.internal.JavaGradlePluginDevelopmentPlugin"
            description = "Fast track development of Gradle plugins in Java"
        }
        create("groovyGradlePluginDevelopment") {
            id = "dev.gradleplugins.groovy-gradle-plugin"
            implementationClass = "dev.gradleplugins.internal.GroovyGradlePluginDevelopmentPlugin"
            description = "Fast track development of Gradle plugins in Groovy"
        }
        create("kotlinGradlePluginDevelopment") {
            id = "dev.gradleplugins.kotlin-gradle-plugin"
            implementationClass = "dev.gradleplugins.internal.KotlinGradlePluginDevelopmentPlugin"
            description = "Fast track development of Gradle plugins in Kotlin"
        }
    }
}

pluginBundle {
    website = "https://gradleplugins.dev/"
//    description = "A sets of highly opinionated plugins to kick start any Gradle plugin project."
    tags = listOf("gradle", "gradle-plugins", "development")

    plugins {
        val javaGradlePluginDevelopment by existing {
            // id is captured from java-gradle-plugin configuration
            displayName = "Fast Gradle plugin development in Java"
            tags = listOf("gradle", "gradle-plugins", "development", "java")
        }
        val groovyGradlePluginDevelopment by existing {
            // id is captured from java-gradle-plugin configuration
            displayName = "Fast Gradle plugin development in Groovy"
            tags = listOf("gradle", "gradle-plugins", "development", "groovy")
        }
        val kotlinGradlePluginDevelopment by existing {
            // id is captured from java-gradle-plugin configuration
            displayName = "Fast Gradle plugin development in Kotlin"
            tags = listOf("gradle", "gradle-plugins", "development", "kotlin")
        }
    }
}

tasks.register("release") {
    dependsOn("publishPlugins")
}