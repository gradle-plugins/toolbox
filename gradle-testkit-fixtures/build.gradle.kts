plugins {
    `groovy`
    dev.gradleplugins.shaded
    dev.gradleplugins.artifacts
    dev.gradleplugins.publishing
}

group = "dev.gradleplugins"
version = "0.0.1"
description = "Gradle TestKit fixtures for fast and efficient Gradle plugin development."

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation("org.spockframework:spock-core:1.2-groovy-2.5") {
        exclude(group = "org.codehaus.groovy")
    }
    shaded("commons-io:commons-io:2.6")
    implementation(gradleTestKit())
}
