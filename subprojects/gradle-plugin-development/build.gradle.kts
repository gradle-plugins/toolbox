plugins {
    id("dev.gradleplugins.java-gradle-plugin")
    dev.gradleplugins.experimental.`shaded-artifact`
    `maven-publish`
}

// Supported by the development plugins
tasks.named("publishPlugins") {
    dependsOn("shadowJar")
}
tasks.named("jar") {
    enabled = false
}
afterEvaluate {
    publishing {
        publications {
            withType<MavenPublication> {
                if (name == "pluginMaven") {
                    setArtifacts(listOf(tasks.getByName("shadowJar")))
                }
            }
        }
    }
}

repositories {
    jcenter()
    gradlePluginPortal()
}

val stubConfiguration = configurations.create("stub") {
    isVisible = false
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "stub"))
}

dependencies {
    add(stubConfiguration.name, project(":gradle-plugin-development-stubs"))

    implementation(gradleApi())
    implementation("com.gradle.publish:plugin-publish-plugin:0.10.1")
    functionalTestImplementation(project(":gradle-testkit-fixtures"))
    compileOnly(project(":gradle-plugin-development-annotation"))
    shaded("org.ow2.asm:asm:6.0")
    shaded("org.ow2.asm:asm-util:6.0")

    // TODO: should be inherited from implementation
    functionalTestImplementation(project(":gradle-plugin-development-annotation"))
//    functionalTestImplementation("com.gradle.publish:plugin-publish-plugin:0.10.1")
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
    getPluginClasspath().from(configurations.shaded.get())
}

sourceSets {
    main.configure {
        this.resources.srcDir(stubConfiguration)
    }
}

fun withoutSnapshot(version: String): String {
    return version.toString().replace("-SNAPSHOT", "")
}

val generatorTask = tasks.register("createVersionInformation") {
    outputs.dir(project.layout.buildDirectory.dir("generatedSources"))
    inputs.property("version", project.version)
    inputs.property("group", project.group)
    inputs.property("name", project.project(":gradle-testkit-fixtures").name)
    doLast {
        project.layout.buildDirectory.file("generatedSources/TestFixtures.java").get().asFile.writeText("""package dev.gradleplugins.internal;

public class TestFixtures {
    public static final boolean released = ${!project.version.toString().contains("-SNAPSHOT")};
    public static final String notation = "${project.group}:${project.project(":gradle-testkit-fixtures").name}:${project.version}";
    public static final String apiVersion = "0.0.12";
    public static final String currentVersion = "${project.version}";
}
""")
    }
}
tasks.named<JavaCompile>("compileJava") {
    dependsOn(generatorTask)
}
sourceSets.main.configure {
    java.srcDir(project.layout.buildDirectory.dir("generatedSources"))
}

//gradlePlugin {
//    plugins {
//        create("javaGradlePluginDevelopment") {
//            id = "dev.gradleplugins.java-gradle-plugin"
//            implementationClass = "dev.gradleplugins.internal.JavaGradlePluginDevelopmentPlugin"
//            description = "Fast track development of Gradle plugins in Java"
//        }
//        create("groovyGradlePluginDevelopment") {
//            id = "dev.gradleplugins.groovy-gradle-plugin"
//            implementationClass = "dev.gradleplugins.internal.GroovyGradlePluginDevelopmentPlugin"
//            description = "Fast track development of Gradle plugins in Groovy"
//        }
//        create("kotlinGradlePluginDevelopment") {
//            id = "dev.gradleplugins.kotlin-gradle-plugin"
//            implementationClass = "dev.gradleplugins.internal.KotlinGradlePluginDevelopmentPlugin"
//            description = "Fast track development of Gradle plugins in Kotlin"
//        }
//    }
//}

pluginBundle {
    website = "https://gradleplugins.dev/"
//    description = "A sets of highly opinionated plugins to kick start any Gradle plugin project."
    tags = listOf("gradle", "gradle-plugins", "development")

    plugins {
        val javaGradlePluginDevelopment by creating {
            id = "dev.gradleplugins.java-gradle-plugin"
            description = "Fast track development of Gradle plugins in Java"
            displayName = "Fast Gradle plugin development in Java"
            tags = listOf("gradle", "gradle-plugins", "development", "java")
        }
        val groovyGradlePluginDevelopment by creating {
            id = "dev.gradleplugins.groovy-gradle-plugin"
            description = "Fast track development of Gradle plugins in Groovy"
            displayName = "Fast Gradle plugin development in Groovy"
            tags = listOf("gradle", "gradle-plugins", "development", "groovy")
        }
        val kotlinGradlePluginDevelopment by creating {
            id = "dev.gradleplugins.kotlin-gradle-plugin"
            description = "Fast track development of Gradle plugins in Kotlin"
            displayName = "Fast Gradle plugin development in Kotlin"
            tags = listOf("gradle", "gradle-plugins", "development", "kotlin")
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

tasks.register("release") {
    dependsOn("publishPlugins")
}

shadedArtifact {
    packagesToRelocate.set(listOf("org.objectweb.asm", "org.objectweb.asm.util"))
}