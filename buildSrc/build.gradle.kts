plugins {
    `kotlin-dsl`
    `java-gradle-plugin`

    // This is a failed attempt to use the dev plugins
//    id("dev.gradleplugins.kotlin-gradle-plugin") version "0.0.21"
//    id("org.jetbrains.kotlin.jvm") version "1.3.50"
}

// The following should be replaced by the dev plugins
kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

gradlePlugin {
    plugins {
        register("publishing-plugin") {
            id = "dev.gradleplugins.experimental.publishing"
            implementationClass = "dev.gradleplugins.PublishingPlugin"
        }
        register("publishing-base-plugin") {
            id = "dev.gradleplugins.experimental.publishing-base"
            implementationClass = "dev.gradleplugins.PublishingBasePlugin"
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
        register("setup-plugin") {
            id = "dev.gradleplugins.experimental.setup"
            implementationClass = "dev.gradleplugins.SetupProjectPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin"))
}

// Non dev plugin specific configuration
repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    implementation("com.github.jengelman.gradle.plugins:shadow:5.1.0")
    implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:0.4.2")
    implementation("com.gradle.publish:plugin-publish-plugin:0.10.1")
}
