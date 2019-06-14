plugins {
    `groovy`
    dev.gradleplugins.artifacts
    dev.gradleplugins.publishing
}

group = "dev.gradleplugins"
version = "0.1.0-SNAPSHOT"
description = "Gradle TestKit fixtures for fast and efficient Gradle plugin development."

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation("org.spockframework:spock-core:1.2-groovy-2.5") {
        exclude(group = "org.codehaus.groovy")
    }
    implementation("com.gradle.publish:plugin-publish-plugin:0.10.1")
    implementation("commons-io:commons-io:2.6")
    implementation(gradleTestKit())
}
