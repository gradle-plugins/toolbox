import dev.gradleplugins.GitHubSourceContolManagerPlugin
import dev.gradleplugins.GitHubSourceControlManagerExtension
import dev.gradleplugins.OpenSourceSoftwareLicensePlugin

plugins {
    dev.gradleplugins.ide
}

allprojects {
    group = "dev.gradleplugins"
    version = "0.0.5-SNAPSHOT"

    apply<GitHubSourceContolManagerPlugin>()
    configure<GitHubSourceControlManagerExtension> {
        gitHubOrganization.set("gradle-plugins")
    }

    apply<OpenSourceSoftwareLicensePlugin>()
}