import dev.gradleplugins.GitHubSourceContolManagerPlugin
import dev.gradleplugins.GitHubSourceControlManagerExtension

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
}