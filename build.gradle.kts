import dev.gradleplugins.GitHubSourceControlManagerPlugin
import dev.gradleplugins.GitHubSourceControlManagerExtension
import dev.gradleplugins.OpenSourceSoftwareLicensePlugin

plugins {
    dev.gradleplugins.experimental.ide
}

allprojects {
    group = "dev.gradleplugins"
    version = "0.0.5-SNAPSHOT"

    apply<GitHubSourceControlManagerPlugin>()
    configure<GitHubSourceControlManagerExtension> {
        gitHubOrganization.set("gradle-plugins")
    }

    apply<OpenSourceSoftwareLicensePlugin>()
}