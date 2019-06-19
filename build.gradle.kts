import dev.gradleplugins.GitHubSourceControlManagerPlugin
import dev.gradleplugins.GitHubSourceControlManagerExtension
import dev.gradleplugins.OpenSourceSoftwareLicensePlugin

plugins {
    dev.gradleplugins.experimental.ide
    //id("dev.gradleplugins.java-gradle-plugin") version "0.0.5" apply false
}

allprojects {
    group = "dev.gradleplugins"
    version = "0.0.6"

    apply<GitHubSourceControlManagerPlugin>()
    configure<GitHubSourceControlManagerExtension> {
        gitHubOrganization.set("gradle-plugins")
    }

    apply<OpenSourceSoftwareLicensePlugin>()
}