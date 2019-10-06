import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    groovy
    dev.gradleplugins.experimental.`shaded-artifact`
    dev.gradleplugins.experimental.artifacts
    dev.gradleplugins.experimental.publishing
}

description = "Gradle TestKit fixtures for fast and efficient Gradle plugin development."

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    // This is tricky as it's a API dependency but isn't published anywhere
    //   Let's put the burden on the user to declare that as well
    //   They should be declaring that dependency anyway as this library is not meant to be used outside of the Gradle plugin development plugins within Gradle runtime.
    implementation(gradleTestKit())

    // This is tricky as it's a API dependency but may work for other versions
    //    Let's put the burden on the user to declare his requirements but also assume the code is compatible with older versions ;)
    // TODO: At some point, we will need make this work with an "older" version of Spock depending on how backward compatible the Gradle plugin development plugins will support.
    implementation("org.spockframework:spock-core:1.2-groovy-2.5")
}

configure<PublishingExtension> {
    publications {
        named<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

afterEvaluate {
    configure<BintrayExtension> {
        pkg(closureOf<BintrayExtension.PackageConfig> {
            name = "testkit-fixtures"
        })
    }
}

tasks.register("release") {
    dependsOn("bintrayUpload")
}

// TODO: fix all javadoc issues
