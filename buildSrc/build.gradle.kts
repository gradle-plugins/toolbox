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
    implementation("com.gradle.publish:plugin-publish-plugin:0.10.1")
}

gradlePlugin {
    plugins {
        register("publishing-plugin") {
            id = "dev.gradleplugins.experimental.publishing"
            implementationClass = "dev.gradleplugins.PublishingPlugin"
        }
        register("ide-plugin") {
            id = "dev.gradleplugins.experimental.ide"
            implementationClass = "dev.gradleplugins.IdePlugin"
        }
        register("additional-artifacts-plugin") {
            id = "dev.gradleplugins.experimental.artifacts"
            implementationClass = "dev.gradleplugins.AdditionalArtifactsPlugin"
        }
        register("shaded-artifacts-plugin") {
            id = "dev.gradleplugins.experimental.shaded-artifact"
            implementationClass = "dev.gradleplugins.ShadedArtifactPlugin"
        }
        register("github-plugin") {
            id = "dev.gradleplugins.experimental.scm.github"
            implementationClass = "dev.gradleplugins.GitHubSourceControlManagerPlugin"
        }
        register("oss-license-plugin") {
            id = "dev.gradleplugins.experimental.license"
            implementationClass = "dev.gradleplugins.OpenSourceSoftwareLicensePlugin"
        }
    }
}