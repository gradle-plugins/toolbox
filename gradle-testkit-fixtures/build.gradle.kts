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
    shaded("commons-io:commons-io:2.6")

    // This is tricky as it's a API dependency but isn't published anywhere
    //   Let's put the burden on the user to declare that as well
    implementation(gradleTestKit())

    // This is tricky as it's a API dependency but may work for other versions
    //    Let's put the burden on the user to declare his requirements but also assume the code is compatible with older versions ;)
    implementation("org.spockframework:spock-core:1.2-groovy-2.5") {
        exclude(group = "org.codehaus.groovy")
    }
}

shadedArtifact {
    packagesToRelocate.set(listOf("org.apache.commons.io"))
}
