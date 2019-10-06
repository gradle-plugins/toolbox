import dev.gradleplugins.GitHubSourceControlManagerPlugin
import dev.gradleplugins.GitHubSourceControlManagerExtension
import dev.gradleplugins.OpenSourceSoftwareLicensePlugin

plugins {
    id("com.gradle.build-scan") version "2.3"
    dev.gradleplugins.experimental.ide
    dev.gradleplugins.experimental.setup
    id("dev.gradleplugins.java-gradle-plugin") version "0.0.14" apply false
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlways()
}

allprojects {
    group = "dev.gradleplugins"
    version = "0.0.16-SNAPSHOT"

    apply<GitHubSourceControlManagerPlugin>()
    configure<GitHubSourceControlManagerExtension> {
        gitHubOrganization.set("gradle-plugins")
    }

    apply<OpenSourceSoftwareLicensePlugin>()
}

// TODO: Fails the build if running with anything else than Java 8 (or fix the projects so we can execute the build on other Java version)