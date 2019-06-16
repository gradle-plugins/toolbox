plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    jcenter()
    gradlePluginPortal()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    implementation("com.github.jengelman.gradle.plugins:shadow:5.0.0")
    implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:0.4.2")
}

gradlePlugin {
    plugins {
        register("publishing-plugin") {
            id = "dev.gradleplugins.publishing"
            implementationClass = "dev.gradleplugins.PublishingPlugin"
        }
        register("ide-plugin") {
            id = "dev.gradleplugins.ide"
            implementationClass = "dev.gradleplugins.IdePlugin"
        }
        register("additional-artifacts-plugin") {
            id = "dev.gradleplugins.artifacts"
            implementationClass = "dev.gradleplugins.AdditionalArtifactsPlugin"
        }
        register("shaded-artifacts-plugin") {
            id = "dev.gradleplugins.shaded-artifact"
            implementationClass = "dev.gradleplugins.ShadedArtifactPlugin"
        }
    }
}