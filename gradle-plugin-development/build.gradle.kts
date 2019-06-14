plugins {
    `kotlin-dsl`
    `groovy`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version("0.10.1")
}

group = "dev.gradleplugins"
version = "0.1.0-SNAPSHOT"

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("org.spockframework:spock-core:1.2-groovy-2.5") {
        exclude(group = "org.codehaus.groovy")
    }
    implementation("com.gradle.publish:plugin-publish-plugin:0.10.1")
    implementation("commons-io:commons-io:2.6")
    implementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("javaGradlePluginDevelopment") {
            id = "dev.gradleplugins.java-gradle-plugin"
            implementationClass = "dev.gradleplugins.internal.JavaGradlePluginDevelopmentPlugin"
        }
        create("groovyGradlePluginDevelopment") {
            id = "dev.gradleplugins.groovy-gradle-plugin"
            implementationClass = "dev.gradleplugins.internal.GroovyGradlePluginDevelopmentPlugin"
        }
        create("kotlinGradlePluginDevelopment") {
            id = "dev.gradleplugins.kotlin-gradle-plugin"
            implementationClass = "dev.gradleplugins.internal.KotlinGradlePluginDevelopmentPlugin"
        }
    }
}

pluginBundle {
    website = "https://gradleplugins.dev/"
    vcsUrl = "https://github.com/gradle-plugins/development-gradle-plugin"
    description = "Sets of highly opinionated plugins to kick start any."
    tags = listOf("gradle", "gradle-plugins", "development")

    plugins {
        val javaGradlePluginDevelopment by existing {
            // id is captured from java-gradle-plugin configuration
            displayName = "Visual Studio Code Gradle Plugin"
        }
        val groovyGradlePluginDevelopment by existing {
            // id is captured from java-gradle-plugin configuration
            displayName = "Visual Studio Code Gradle Plugin"
        }
        val kotlinGradlePluginDevelopment by existing {
            // id is captured from java-gradle-plugin configuration
            displayName = "Visual Studio Code Gradle Plugin"
        }
    }
}